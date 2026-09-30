package com.banlinhkien.service;

import com.banlinhkien.dto.PcBuilderItemDto;
import com.banlinhkien.dto.PcBuilderSessionDto;
import com.banlinhkien.dto.PcComponentDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.ProductSpecRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PcBuilderService {

    public static final String SESSION_KEY = "buildpc";

    /**
     * Standard 7-step Hardware Category Map
     * 1: CPU, 3: Mainboard, 2: RAM, 4: VGA, 5: SSD/HDD, 6: PSU, 7: Case
     */
    public static final Map<Integer, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put(1, "Vi xử lý (CPU)");
        CATEGORIES.put(3, "Bo mạch chủ (Mainboard)");
        CATEGORIES.put(2, "Bộ nhớ trong (RAM)");
        CATEGORIES.put(4, "Card màn hình (VGA)");
        CATEGORIES.put(5, "Ổ cứng (SSD/HDD)");
        CATEGORIES.put(6, "Nguồn máy tính (PSU)");
        CATEGORIES.put(7, "Vỏ máy tính (Case)");
    }

    private final ProductRepository productRepository;
    private final ProductSpecRepository productSpecRepository;

    public record SelectResult(boolean success, String message, String warning, PcBuilderSessionDto build) {}
    public record BatchAddResult(boolean success, String message, List<String> stockErrors, int cartCount) {}

    /**
     * Normalize socket strings to allow robust space-insensitive comparisons.
     * e.g. "LGA 1700", "LGA-1700", "lga_1700" -> "LGA1700"
     */
    public static String normalizeSocket(String socket) {
        if (socket == null || socket.isBlank()) {
            return "";
        }
        return socket.toUpperCase().replaceAll("[\\s\\-_]", "").trim();
    }

    /**
     * Get active build session, refreshing product data from DB.
     */
    public PcBuilderSessionDto getBuildSession(HttpSession session) {
        PcBuilderSessionDto build = (PcBuilderSessionDto) session.getAttribute(SESSION_KEY);
        if (build == null) {
            build = new PcBuilderSessionDto();
            session.setAttribute(SESSION_KEY, build);
        }

        // Refresh existing items with current product stock and price
        Map<Integer, PcBuilderItemDto> refreshed = new LinkedHashMap<>();
        for (Map.Entry<Integer, PcBuilderItemDto> entry : build.getItems().entrySet()) {
            Integer catId = entry.getKey();
            PcBuilderItemDto item = entry.getValue();
            if (item != null && item.getProductId() != null) {
                Optional<Product> prodOpt = productRepository.findById(item.getProductId());
                if (prodOpt.isPresent() && Boolean.TRUE.equals(prodOpt.get().getIsActive())) {
                    Product prod = prodOpt.get();
                    String socket = productSpecRepository.findSpecValue(prod.getId(), "Socket")
                            .orElse(item.getSocket() != null ? item.getSocket() : "");

                    int qty = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
                    int stock = prod.getQuantity() != null ? prod.getQuantity() : 0;

                    refreshed.put(catId, PcBuilderItemDto.builder()
                            .id(prod.getId())
                            .productId(prod.getId())
                            .name(prod.getName())
                            .price(prod.getFinalPrice())
                            .originalPrice(prod.getPrice())
                            .discountPercent(prod.getDiscountPercent())
                            .image(prod.getImage())
                            .quantity(qty)
                            .inStock(stock > 0)
                            .socket(socket)
                            .build());
                }
            }
        }
        build.setItems(refreshed);
        session.setAttribute(SESSION_KEY, build);
        return build;
    }

    /**
     * Select a component into the buildpc session with bidirectional socket auto-eviction.
     */
    public SelectResult selectComponent(HttpSession session, Integer categoryId, Long productId, Integer quantity) {
        if (categoryId == null || !CATEGORIES.containsKey(categoryId)) {
            return new SelectResult(false, "Danh mục linh kiện không hợp lệ.", null, null);
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || Boolean.FALSE.equals(product.getIsActive())) {
            return new SelectResult(false, "Sản phẩm không tồn tại hoặc đã ngừng bán.", null, null);
        }

        PcBuilderSessionDto build = getBuildSession(session);
        String socketSpec = productSpecRepository.findSpecValue(productId, "Socket").orElse("");
        String warning = null;

        // Bidirectional Socket Auto-Eviction Logic
        if (categoryId == 1) { // CPU selected
            if (build.getItems().containsKey(3)) { // Check existing Mainboard
                PcBuilderItemDto mbItem = build.getItems().get(3);
                String mbSocket = mbItem.getSocket();
                if (mbSocket == null || mbSocket.isBlank()) {
                    mbSocket = productSpecRepository.findSpecValue(mbItem.getProductId(), "Socket").orElse("");
                }

                if (!socketSpec.isBlank() && !mbSocket.isBlank() &&
                    !normalizeSocket(socketSpec).equals(normalizeSocket(mbSocket))) {
                    build.getItems().remove(3); // Auto-evict incompatible Mainboard
                    warning = "Bo mạch chủ trước đó đã được gỡ bỏ do không tương thích chuẩn Socket.";
                    log.info("Auto-evicted Mainboard #{} due to socket mismatch (CPU: {}, MB: {})",
                            mbItem.getProductId(), socketSpec, mbSocket);
                }
            }
        } else if (categoryId == 3) { // Mainboard selected
            if (build.getItems().containsKey(1)) { // Check existing CPU
                PcBuilderItemDto cpuItem = build.getItems().get(1);
                String cpuSocket = cpuItem.getSocket();
                if (cpuSocket == null || cpuSocket.isBlank()) {
                    cpuSocket = productSpecRepository.findSpecValue(cpuItem.getProductId(), "Socket").orElse("");
                }

                if (!socketSpec.isBlank() && !cpuSocket.isBlank() &&
                    !normalizeSocket(socketSpec).equals(normalizeSocket(cpuSocket))) {
                    build.getItems().remove(1); // Auto-evict incompatible CPU
                    warning = "Vi xử lý (CPU) trước đó đã được gỡ bỏ do không tương thích chuẩn Socket.";
                    log.info("Auto-evicted CPU #{} due to socket mismatch (MB: {}, CPU: {})",
                            cpuItem.getProductId(), socketSpec, cpuSocket);
                }
            }
        }

        int qty = quantity != null && quantity > 0 ? quantity : 1;
        int stock = product.getQuantity() != null ? product.getQuantity() : 0;

        PcBuilderItemDto item = PcBuilderItemDto.builder()
                .id(product.getId())
                .productId(product.getId())
                .name(product.getName())
                .price(product.getFinalPrice())
                .originalPrice(product.getPrice())
                .discountPercent(product.getDiscountPercent())
                .image(product.getImage())
                .quantity(qty)
                .inStock(stock > 0)
                .socket(socketSpec)
                .build();

        build.getItems().put(categoryId, item);
        session.setAttribute(SESSION_KEY, build);

        return new SelectResult(true, "Đã thêm linh kiện vào cấu hình.", warning, build);
    }

    /**
     * Remove an item from the buildpc session.
     */
    public PcBuilderSessionDto removeComponent(HttpSession session, Integer categoryId) {
        PcBuilderSessionDto build = getBuildSession(session);
        if (categoryId != null && build.getItems().containsKey(categoryId)) {
            build.getItems().remove(categoryId);
            session.setAttribute(SESSION_KEY, build);
        }
        return build;
    }

    /**
     * Clear all configured parts from the buildpc session.
     */
    public void clearBuild(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }

    /**
     * Get products for a hardware category slot, filtered by active CPU/Mainboard socket.
     */
    public List<PcComponentDto> getComponentsForCategory(Integer categoryId, String requiredSocket) {
        if (categoryId == null || !CATEGORIES.containsKey(categoryId)) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(Long.valueOf(categoryId));

        // Sort by price ascending
        products.sort(Comparator.comparing(Product::getFinalPrice));

        String normRequired = normalizeSocket(requiredSocket);
        List<PcComponentDto> result = new ArrayList<>();

        for (Product p : products) {
            String sock = productSpecRepository.findSpecValue(p.getId(), "Socket").orElse("");

            if (!normRequired.isBlank()) {
                String normProdSock = normalizeSocket(sock);
                if (!normRequired.equals(normProdSock)) {
                    continue; // Skip incompatible socket
                }
            }

            result.add(PcComponentDto.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .price(p.getFinalPrice())
                    .originalPrice(p.getPrice())
                    .discountPercent(p.getDiscountPercent())
                    .quantity(p.getQuantity() != null ? p.getQuantity() : 0)
                    .image(p.getImage())
                    .socket(sock)
                    .build());
        }

        return result;
    }

    /**
     * Batch add all configured components into shopping cart.
     */
    public BatchAddResult batchAddToCart(HttpSession session, CartService cartService) {
        PcBuilderSessionDto build = getBuildSession(session);
        if (build.getItems().isEmpty()) {
            return new BatchAddResult(false, "Chưa có linh kiện nào trong cấu hình.", Collections.emptyList(), 0);
        }

        List<String> stockErrors = new ArrayList<>();
        for (PcBuilderItemDto item : build.getItems().values()) {
            Product prod = productRepository.findById(item.getProductId()).orElse(null);
            if (prod == null || prod.getQuantity() == null || prod.getQuantity() <= 0) {
                String name = prod != null ? prod.getName() : item.getName();
                stockErrors.add(name + " đã hết hàng.");
            }
        }

        if (!stockErrors.isEmpty()) {
            return new BatchAddResult(false, "Không thể thêm vào giỏ: " + String.join(", ", stockErrors), stockErrors, 0);
        }

        for (PcBuilderItemDto item : build.getItems().values()) {
            try {
                int qty = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
                cartService.addItem(session, item.getProductId(), qty);
            } catch (Exception e) {
                log.warn("PcBuilderService batchAddToCart: Could not add item #{}: {}", item.getProductId(), e.getMessage());
            }
        }

        int count = cartService.getTotalCount(session);
        return new BatchAddResult(true, "Đã thêm toàn bộ cấu hình PC vào giỏ hàng.", Collections.emptyList(), count);
    }
}
