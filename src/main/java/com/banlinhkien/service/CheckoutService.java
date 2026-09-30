package com.banlinhkien.service;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CartItemDto;
import com.banlinhkien.dto.CheckoutRequestDto;
import com.banlinhkien.dto.VoucherResultDto;
import com.banlinhkien.entity.*;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.enums.PaymentMethod;
import com.banlinhkien.enums.PaymentStatus;
import com.banlinhkien.enums.WarehouseLogType;
import com.banlinhkien.exception.InsufficientStockException;
import com.banlinhkien.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseLogRepository warehouseLogRepository;
    private final UserAddressRepository userAddressRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    private final ShippingService shippingService;

    /**
     * Executes order checkout in an atomic transaction with strict pessimistic locking.
     * Guaranteed:
     * 1. Product IDs sorted ASC before locking to avoid deadlocks.
     * 2. findByIdForUpdate (lock) -> check stock -> deduct stock.
     * 3. Snapshot prices, order items, warehouse export logs, and voucher usage.
     */
    @Transactional
    public Order placeOrder(CheckoutRequestDto request, CartDto cart, User currentUser) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
        }

        Collection<CartItemDto> cartItems = cart.getItemList();

        // Step 1: Quantity aggregation by product ID
        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();
        for (CartItemDto item : cartItems) {
            Long pId = item.getProductId();
            if (pId == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Mục trong giỏ hàng không hợp lệ.");
            }
            requestedQuantities.put(pId, requestedQuantities.getOrDefault(pId, 0) + item.getQuantity());
        }

        // Step 2: Sort product IDs ASC to prevent multi-resource deadlocks
        List<Long> sortedProductIds = requestedQuantities.keySet().stream().sorted().toList();

        // Step 3: Lock each product and verify stock sequentially
        Map<Long, Product> lockedProducts = new HashMap<>();
        for (Long productId : sortedProductIds) {
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new NoSuchElementException("Sản phẩm không tồn tại (ID: " + productId + ")."));

            if (Boolean.FALSE.equals(product.getIsActive())) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' đã ngừng kinh doanh hoặc tạm khóa.");
            }

            int requiredQty = requestedQuantities.get(productId);
            int availableStock = product.getQuantity() != null ? product.getQuantity() : 0;

            if (availableStock < requiredQty) {
                throw new InsufficientStockException("Sản phẩm '" + product.getName() + "' chỉ còn "
                        + availableStock + " cái trong kho, không đủ số lượng đặt hàng (" + requiredQty + ").");
            }

            lockedProducts.put(productId, product);
        }

        // Step 4: Atomically decrement stock
        for (Long productId : sortedProductIds) {
            Product product = lockedProducts.get(productId);
            int qtyToDeduct = requestedQuantities.get(productId);
            product.setQuantity(product.getQuantity() - qtyToDeduct);
            productRepository.save(product);
        }

        // Step 5: Authoritative calculation of subtotal, shipping, voucher discount
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemDto item : cartItems) {
            Product product = lockedProducts.get(item.getProductId());
            BigDecimal unitPrice = product.getFinalPrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        String province = request.getProvince();
        BigDecimal shippingFee = shippingService.calculateFee(province, subtotal, cartItems.size());

        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher appliedVoucher = null;
        String voucherCode = request.getVoucherCode();

        Long userId = currentUser != null ? currentUser.getId() : null;
        if (voucherCode != null && !voucherCode.isBlank()) {
            VoucherResultDto voucherResult = voucherService.validate(voucherCode, subtotal, userId);
            if (voucherResult.isSuccess()) {
                appliedVoucher = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(voucherCode.trim()).orElse(null);
                discountAmount = voucherResult.getDiscount();
            }
        } else if (cart.getVoucherDiscount() != null && cart.getVoucherDiscount().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = cart.getVoucherDiscount();
            voucherCode = cart.getAppliedVoucher();
            if (voucherCode != null) {
                appliedVoucher = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(voucherCode.trim()).orElse(null);
            }
        }

        BigDecimal finalAmount = subtotal.add(shippingFee).subtract(discountAmount).max(BigDecimal.ZERO);

        PaymentMethod paymentMethod = PaymentMethod.cod;
        if (request.getPaymentMethod() != null) {
            try {
                paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toLowerCase());
            } catch (IllegalArgumentException e) {
                paymentMethod = PaymentMethod.cod;
            }
        }

        // Step 6: Create Order record
        Order order = Order.builder()
                .userId(userId)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null)
                .customerAddress(request.getCustomerAddress().trim())
                .shippingProvince(province)
                .totalAmount(finalAmount)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .voucherCode(appliedVoucher != null ? appliedVoucher.getCode() : (voucherCode != null && !voucherCode.isBlank() ? voucherCode : null))
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.unpaid)
                .status(OrderStatus.pending)
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Step 7: Create OrderItems and WarehouseLogs
        for (CartItemDto item : cartItems) {
            Product product = lockedProducts.get(item.getProductId());
            BigDecimal unitPrice = product.getFinalPrice();

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(unitPrice)
                    .quantity(item.getQuantity())
                    .build();
            orderItemRepository.save(orderItem);

            WarehouseLog logEntry = WarehouseLog.builder()
                    .productId(product.getId())
                    .type(WarehouseLogType.EXPORT)
                    .quantity(item.getQuantity())
                    .referenceId(savedOrder.getId())
                    .note("Xuất kho đơn hàng #" + savedOrder.getId())
                    .createdBy(userId)
                    .createdAt(LocalDateTime.now())
                    .warehouseId(product.getWarehouseId() != null ? product.getWarehouseId() : 1L)
                    .reason("sale")
                    .build();
            warehouseLogRepository.save(logEntry);
        }

        // Step 8: Record Voucher Usage if applied
        if (appliedVoucher != null) {
            voucherService.recordUsage(appliedVoucher, userId, savedOrder.getId());
        }

        // Step 9: Save address to address book if requested
        if (userId != null && Boolean.TRUE.equals(request.getSaveAddress())) {
            try {
                UserAddress address = UserAddress.builder()
                        .userId(userId)
                        .fullName(request.getCustomerName().trim())
                        .phone(request.getCustomerPhone().trim())
                        .province(province != null ? province : "")
                        .district(request.getDistrict() != null ? request.getDistrict() : "")
                        .ward(request.getWard())
                        .addressDetail(request.getCustomerAddress().trim())
                        .isDefault(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                userAddressRepository.save(address);
            } catch (Exception e) {
                log.warn("Failed to save user address: {}", e.getMessage());
            }
        }

        // Clear cart items
        cart.clear();

        return savedOrder;
    }
}
