package com.banlinhkien.service;

import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductImageInitializer {

    private final ProductRepository productRepository;

    public static final Map<Long, String> PRODUCT_IMAGE_MAP = new HashMap<>();

    static {
        // 1. CPU
        PRODUCT_IMAGE_MAP.put(1L, "intel-i5-12400f.png");
        PRODUCT_IMAGE_MAP.put(3L, "ryzen-5-5600x.png");
        PRODUCT_IMAGE_MAP.put(4L, "ryzen-7-5700x.png");
        PRODUCT_IMAGE_MAP.put(5L, "intel-i7-12700f.png");
        PRODUCT_IMAGE_MAP.put(20L, "intel-i3-12100f.png");
        PRODUCT_IMAGE_MAP.put(21L, "intel-i5-12400f.png");
        PRODUCT_IMAGE_MAP.put(22L, "intel-i5-13400f.png");
        PRODUCT_IMAGE_MAP.put(23L, "intel-i7-12700f.png");
        PRODUCT_IMAGE_MAP.put(24L, "intel-i7-13700f.png");
        PRODUCT_IMAGE_MAP.put(25L, "intel-i9-13900f.png");
        PRODUCT_IMAGE_MAP.put(26L, "ryzen-5-7600x.png");
        PRODUCT_IMAGE_MAP.put(27L, "ryzen-7-7700x.png");
        PRODUCT_IMAGE_MAP.put(28L, "ryzen-9-7900x.png");
        PRODUCT_IMAGE_MAP.put(29L, "ryzen-5-5500.png");
        PRODUCT_IMAGE_MAP.put(30L, "ryzen-7-5700x.png");

        // 2. Mainboards
        PRODUCT_IMAGE_MAP.put(2L, "asus-tuf-b760m-plus.png");
        PRODUCT_IMAGE_MAP.put(9L, "msi-mag-b550-tomahawk.png");
        PRODUCT_IMAGE_MAP.put(10L, "gigabyte-b760m-ds3h.png");
        PRODUCT_IMAGE_MAP.put(31L, "asus-prime-b660m-a.png");
        PRODUCT_IMAGE_MAP.put(32L, "msi-pro-b660m-a.png");
        PRODUCT_IMAGE_MAP.put(33L, "gigabyte-b660m-ds3h.png");
        PRODUCT_IMAGE_MAP.put(34L, "asus-rog-strix-b660-f.png");
        PRODUCT_IMAGE_MAP.put(35L, "msi-mag-z690-tomahawk.png");
        PRODUCT_IMAGE_MAP.put(36L, "asus-prime-b650-plus.png");
        PRODUCT_IMAGE_MAP.put(37L, "gigabyte-b650-aorus-elite-ax.png");
        PRODUCT_IMAGE_MAP.put(38L, "msi-mpg-x670e-carbon-wifi.png");
        PRODUCT_IMAGE_MAP.put(39L, "gigabyte-b550m-ds3h.png");
        PRODUCT_IMAGE_MAP.put(40L, "asus-tuf-b550-plus.png");

        // 3. RAM
        PRODUCT_IMAGE_MAP.put(6L, "kingston-fury-beast-ddr4-8gb.png");
        PRODUCT_IMAGE_MAP.put(7L, "kingston-fury-beast-ddr4-16gb.png");
        PRODUCT_IMAGE_MAP.put(8L, "corsair-vengeance-ddr5-32gb.png");
        PRODUCT_IMAGE_MAP.put(41L, "kingston-fury-beast-ddr4-8gb.png");
        PRODUCT_IMAGE_MAP.put(42L, "kingston-fury-beast-ddr4-32gb.png");
        PRODUCT_IMAGE_MAP.put(43L, "corsair-vengeance-lpx-16gb.png");
        PRODUCT_IMAGE_MAP.put(44L, "corsair-vengeance-rgb-pro-16gb.png");
        PRODUCT_IMAGE_MAP.put(45L, "gskill-trident-z-rgb-32gb.png");
        PRODUCT_IMAGE_MAP.put(46L, "kingston-fury-beast-ddr5-32gb.png");
        PRODUCT_IMAGE_MAP.put(47L, "corsair-dominator-platinum-rgb-32gb.png");

        // 4. VGA
        PRODUCT_IMAGE_MAP.put(11L, "msi-rtx-4060-gaming-x.png");
        PRODUCT_IMAGE_MAP.put(12L, "gigabyte-rx-7600-gaming-oc.png");
        PRODUCT_IMAGE_MAP.put(48L, "asus-dual-rtx-3060-12gb.png");
        PRODUCT_IMAGE_MAP.put(49L, "msi-rtx-3060-ti-gaming-x.png");
        PRODUCT_IMAGE_MAP.put(50L, "asus-tuf-rtx-3070-oc.png");
        PRODUCT_IMAGE_MAP.put(51L, "gigabyte-rtx-4060-gaming-oc.png");
        PRODUCT_IMAGE_MAP.put(52L, "msi-rtx-4070-ventus-3x.png");
        PRODUCT_IMAGE_MAP.put(53L, "sapphire-pulse-rx-6600.png");
        PRODUCT_IMAGE_MAP.put(54L, "sapphire-nitro-rx-6700-xt.png");
        PRODUCT_IMAGE_MAP.put(55L, "gigabyte-rx-7600-gaming-oc.png");

        // 5. SSD / HDD
        PRODUCT_IMAGE_MAP.put(13L, "wd-blue-sn580-500gb.png");
        PRODUCT_IMAGE_MAP.put(14L, "wd-blue-sn580-1tb.png");
        PRODUCT_IMAGE_MAP.put(15L, "seagate-barracuda-1tb-hdd.png");
        PRODUCT_IMAGE_MAP.put(56L, "samsung-870-evo-500gb.png");
        PRODUCT_IMAGE_MAP.put(57L, "samsung-870-evo-1tb.png");
        PRODUCT_IMAGE_MAP.put(58L, "samsung-980-pro-1tb.png");
        PRODUCT_IMAGE_MAP.put(59L, "wd-blue-sn570-1tb.png");
        PRODUCT_IMAGE_MAP.put(60L, "wd-black-sn850x-2tb.png");
        PRODUCT_IMAGE_MAP.put(61L, "crucial-p3-1tb.png");
        PRODUCT_IMAGE_MAP.put(62L, "seagate-barracuda-1tb-hdd.png");
        PRODUCT_IMAGE_MAP.put(63L, "seagate-barracuda-2tb-hdd.png");

        // 6. PSU
        PRODUCT_IMAGE_MAP.put(16L, "seasonic-focus-gx-650w.png");
        PRODUCT_IMAGE_MAP.put(17L, "corsair-rm750e-750w.png");
        PRODUCT_IMAGE_MAP.put(64L, "coolermaster-mwe-550w-white.png");
        PRODUCT_IMAGE_MAP.put(65L, "coolermaster-mwe-gold-650w.png");
        PRODUCT_IMAGE_MAP.put(66L, "seasonic-focus-gx-750w.png");
        PRODUCT_IMAGE_MAP.put(67L, "seasonic-prime-tx-850w.png");
        PRODUCT_IMAGE_MAP.put(68L, "bequiet-straight-power-11-750w.png");
        PRODUCT_IMAGE_MAP.put(69L, "deepcool-pq650m-650w.png");

        // 7. Case
        PRODUCT_IMAGE_MAP.put(18L, "coolermaster-masterbox-q300l.png");
        PRODUCT_IMAGE_MAP.put(19L, "msi-mag-forge-100r.png");
        PRODUCT_IMAGE_MAP.put(70L, "coolermaster-masterbox-q300l.png");
        PRODUCT_IMAGE_MAP.put(71L, "coolermaster-masterbox-520-mesh.png");
        PRODUCT_IMAGE_MAP.put(72L, "nzxt-h510-flow.png");
        PRODUCT_IMAGE_MAP.put(73L, "nzxt-h7-flow-rgb.png");
        PRODUCT_IMAGE_MAP.put(74L, "lian-li-pc-o11-dynamic-evo.png");
        PRODUCT_IMAGE_MAP.put(75L, "fractal-design-meshify-c.png");
        PRODUCT_IMAGE_MAP.put(76L, "thermaltake-view-71-tg.png");
    }

    public static String getImageForProduct(Long id, String name, Long categoryId) {
        if (id != null && PRODUCT_IMAGE_MAP.containsKey(id)) {
            return PRODUCT_IMAGE_MAP.get(id);
        }
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.contains("i3-12100")) return "intel-i3-12100f.png";
            if (lower.contains("i5-12400") || lower.contains("i5 12400")) return "intel-i5-12400f.png";
            if (lower.contains("i5-13400") || lower.contains("i5 13400")) return "intel-i5-13400f.png";
            if (lower.contains("i7-12700") || lower.contains("i7 12700")) return "intel-i7-12700f.png";
            if (lower.contains("i7-13700") || lower.contains("i7 13700")) return "intel-i7-13700f.png";
            if (lower.contains("i9-13900") || lower.contains("i9 13900")) return "intel-i9-13900f.png";
            if (lower.contains("5600x")) return "ryzen-5-5600x.png";
            if (lower.contains("5700x")) return "ryzen-7-5700x.png";
            if (lower.contains("7600x")) return "ryzen-5-7600x.png";
            if (lower.contains("7700x")) return "ryzen-7-7700x.png";
            if (lower.contains("7900x")) return "ryzen-9-7900x.png";
            if (lower.contains("5500")) return "ryzen-5-5500.png";
            if (lower.contains("tuf") && lower.contains("b760")) return "asus-tuf-b760m-plus.png";
            if (lower.contains("b550") && lower.contains("tomahawk")) return "msi-mag-b550-tomahawk.png";
            if (lower.contains("b760m") && lower.contains("ds3h")) return "gigabyte-b760m-ds3h.png";
            if (lower.contains("z690")) return "msi-mag-z690-tomahawk.png";
            if (lower.contains("x670")) return "msi-mpg-x670e-carbon-wifi.png";
            if (lower.contains("4060") && lower.contains("gaming x")) return "msi-rtx-4060-gaming-x.png";
            if (lower.contains("4060")) return "gigabyte-rtx-4060-gaming-oc.png";
            if (lower.contains("4070")) return "msi-rtx-4070-ventus-3x.png";
            if (lower.contains("3060 ti")) return "msi-rtx-3060-ti-gaming-x.png";
            if (lower.contains("3060")) return "asus-dual-rtx-3060-12gb.png";
            if (lower.contains("3070")) return "asus-tuf-rtx-3070-oc.png";
            if (lower.contains("7600")) return "gigabyte-rx-7600-gaming-oc.png";
            if (lower.contains("6600")) return "sapphire-pulse-rx-6600.png";
            if (lower.contains("6700")) return "sapphire-nitro-rx-6700-xt.png";
            if (lower.contains("sn580") && lower.contains("500")) return "wd-blue-sn580-500gb.png";
            if (lower.contains("sn580")) return "wd-blue-sn580-1tb.png";
            if (lower.contains("980 pro")) return "samsung-980-pro-1tb.png";
            if (lower.contains("870 evo") && lower.contains("500")) return "samsung-870-evo-500gb.png";
            if (lower.contains("870 evo")) return "samsung-870-evo-1tb.png";
            if (lower.contains("sn850x")) return "wd-black-sn850x-2tb.png";
            if (lower.contains("barracuda") && lower.contains("2tb")) return "seagate-barracuda-2tb-hdd.png";
            if (lower.contains("barracuda")) return "seagate-barracuda-1tb-hdd.png";
            if (lower.contains("rm750e")) return "corsair-rm750e-750w.png";
            if (lower.contains("focus gx") && lower.contains("750")) return "seasonic-focus-gx-750w.png";
            if (lower.contains("focus gx")) return "seasonic-focus-gx-650w.png";
            if (lower.contains("prime tx")) return "seasonic-prime-tx-850w.png";
            if (lower.contains("straight power")) return "bequiet-straight-power-11-750w.png";
            if (lower.contains("pq650m")) return "deepcool-pq650m-650w.png";
            if (lower.contains("q300l")) return "coolermaster-masterbox-q300l.png";
            if (lower.contains("forge 100r")) return "msi-mag-forge-100r.png";
            if (lower.contains("h510")) return "nzxt-h510-flow.png";
            if (lower.contains("h7")) return "nzxt-h7-flow-rgb.png";
            if (lower.contains("o11") || lower.contains("dynamic")) return "lian-li-pc-o11-dynamic-evo.png";
            if (lower.contains("meshify")) return "fractal-design-meshify-c.png";
            if (lower.contains("view 71")) return "thermaltake-view-71-tg.png";
            if (lower.contains("fury") && lower.contains("8gb")) return "kingston-fury-beast-ddr4-8gb.png";
            if (lower.contains("fury") && lower.contains("16gb")) return "kingston-fury-beast-ddr4-16gb.png";
            if (lower.contains("fury") && lower.contains("ddr5")) return "kingston-fury-beast-ddr5-32gb.png";
            if (lower.contains("fury")) return "kingston-fury-beast-ddr4-32gb.png";
            if (lower.contains("trident")) return "gskill-trident-z-rgb-32gb.png";
            if (lower.contains("dominator")) return "corsair-dominator-platinum-rgb-32gb.png";
            if (lower.contains("lpx")) return "corsair-vengeance-lpx-16gb.png";
            if (lower.contains("rgb pro")) return "corsair-vengeance-rgb-pro-16gb.png";
            if (lower.contains("vengeance")) return "corsair-vengeance-ddr5-32gb.png";
        }
        if (categoryId != null) {
            if (categoryId == 1L) return "intel-i5-12400f.png";
            if (categoryId == 2L) return "kingston-fury-beast-ddr4-16gb.png";
            if (categoryId == 3L) return "asus-tuf-b760m-plus.png";
            if (categoryId == 4L) return "msi-rtx-4060-gaming-x.png";
            if (categoryId == 5L) return "wd-blue-sn580-1tb.png";
            if (categoryId == 6L) return "seasonic-focus-gx-650w.png";
            if (categoryId == 7L) return "coolermaster-masterbox-q300l.png";
        }
        return "intel-i5-12400f.png";
    }

    @PostConstruct
    @Transactional
    public void syncProductImages() {
        try {
            List<Product> products = productRepository.findAll();
            int updatedCount = 0;

            for (Product product : products) {
                Long id = product.getId();
                String targetImg = getImageForProduct(id, product.getName(), product.getCategory() != null ? product.getCategory().getId() : null);

                if (targetImg != null) {
                    if (product.getImage() == null || product.getImage().trim().isEmpty() || !product.getImage().equals(targetImg)) {
                        product.setImage(targetImg);
                        productRepository.save(product);
                        updatedCount++;
                    }
                }
            }

            if (updatedCount > 0) {
                log.info("Đã tự động cập nhật hình ảnh chính xác cho {} sản phẩm trong hệ thống.", updatedCount);
            }
        } catch (Exception e) {
            log.warn("Không thể đồng bộ hình ảnh sản phẩm lúc khởi động: {}", e.getMessage());
        }
    }
}
