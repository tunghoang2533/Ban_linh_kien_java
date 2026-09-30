package com.banlinhkien.service;

import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.OrderItem;
import com.banlinhkien.entity.Product;
import com.banlinhkien.entity.WarehouseLog;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.enums.PaymentStatus;
import com.banlinhkien.enums.WarehouseLogType;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.WarehouseLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final WarehouseLogRepository warehouseLogRepository;

    @Transactional(readOnly = true)
    public Page<Order> getOrders(OrderStatus status, String search, Pageable pageable) {
        String keyword = (search != null && !search.isBlank()) ? search.trim() : null;
        return orderRepository.searchOrdersAdmin(status, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng #" + id));
    }

    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        if (status == null) {
            return orderRepository.count();
        }
        return orderRepository.countByStatus(status);
    }

    /**
     * Update order status with State Machine validation,
     * Deadlock-free pessimistic locking inventory rollback on cancellation,
     * and financial refund transition.
     */
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus, String trackingCode, Long adminUserId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng #" + orderId));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            if (trackingCode != null && !trackingCode.isBlank()) {
                order.setTrackingCode(trackingCode.trim());
                return orderRepository.save(order);
            }
            return order;
        }

        // State machine rules:
        if (currentStatus == OrderStatus.cancelled) {
            throw new IllegalStateException("Đơn hàng #" + orderId + " đã bị hủy, không thể thay đổi trạng thái.");
        }

        if (newStatus == OrderStatus.cancelled) {
            if (currentStatus == OrderStatus.completed) {
                throw new IllegalStateException("Đơn hàng đã hoàn thành, không thể hủy trực tiếp; vui lòng xử lý qua quy trình Đổi/Trả hàng.");
            }
            if (currentStatus == OrderStatus.shipped) {
                throw new IllegalStateException("Đơn hàng đang giao, không thể hủy trực tiếp.");
            }

            // Rollback inventory with pessimistic locking sorted ASC
            rollbackInventoryForOrder(order, adminUserId);

            // Financial refund check
            if (order.getPaymentStatus() == PaymentStatus.paid) {
                order.setPaymentStatus(PaymentStatus.refunded);
                log.info("Order #{} was marked as PAID. Automatically updated paymentStatus to REFUNDED upon cancellation.", orderId);
            }
        }

        if (trackingCode != null && !trackingCode.isBlank()) {
            order.setTrackingCode(trackingCode.trim());
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order #{} status updated from {} to {} by admin userId: {}", orderId, currentStatus, newStatus, adminUserId);
        return updated;
    }

    private void rollbackInventoryForOrder(Order order, Long adminUserId) {
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        // Aggregate quantities per product in case of duplicates
        Map<Long, Integer> productQuantities = new HashMap<>();
        for (OrderItem item : items) {
            if (item.getProductId() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                productQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            }
        }

        // Sort product IDs ASC to strictly prevent multi-row deadlocks
        List<Long> sortedProductIds = productQuantities.keySet().stream().sorted().toList();

        for (Long productId : sortedProductIds) {
            int restoreQty = productQuantities.get(productId);

            Product product = productRepository.findByIdForUpdate(productId).orElse(null);
            if (product != null) {
                int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
                product.setQuantity(currentQty + restoreQty);
                productRepository.save(product);

                // Create warehouse import audit log
                WarehouseLog logEntry = WarehouseLog.builder()
                        .productId(product.getId())
                        .type(WarehouseLogType.IMPORT)
                        .quantity(restoreQty)
                        .referenceId(order.getId())
                        .note("Hoàn kho do hủy đơn hàng #" + order.getId())
                        .createdBy(adminUserId)
                        .createdAt(LocalDateTime.now())
                        .warehouseId(product.getWarehouseId() != null ? product.getWarehouseId() : 1L)
                        .reason("order_cancel")
                        .build();

                warehouseLogRepository.save(logEntry);
                log.info("Restored {} units to stock for product #{} (Order #{}) with pessimistic lock.",
                        restoreQty, productId, order.getId());
            } else {
                log.warn("Product #{} no longer exists in database during order #{} cancellation stock refund.", productId, order.getId());
            }
        }
    }
}
