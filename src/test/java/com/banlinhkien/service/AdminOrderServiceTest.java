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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseLogRepository warehouseLogRepository;

    @InjectMocks
    private AdminOrderService adminOrderService;

    private Order sampleOrder;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(101L)
                .name("CPU Intel i5")
                .quantity(10)
                .warehouseId(1L)
                .build();

        product2 = Product.builder()
                .id(102L)
                .name("Mainboard B760")
                .quantity(5)
                .warehouseId(1L)
                .build();

        sampleOrder = Order.builder()
                .id(999L)
                .customerName("Nguyễn Văn A")
                .customerPhone("0987654321")
                .status(OrderStatus.pending)
                .paymentStatus(PaymentStatus.unpaid)
                .items(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .order(sampleOrder)
                .productId(101L)
                .productName("CPU Intel i5")
                .price(BigDecimal.valueOf(5000000))
                .quantity(2)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(2L)
                .order(sampleOrder)
                .productId(102L)
                .price(BigDecimal.valueOf(3000000))
                .quantity(1)
                .build();

        sampleOrder.getItems().add(item1);
        sampleOrder.getItems().add(item2);
    }

    @Test
    @DisplayName("Nâng trạng thái từ pending sang processing thành công không hoàn kho")
    void testUpdateStatusToProcessingSuccess() {
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = adminOrderService.updateStatus(999L, OrderStatus.processing, "TRACK123", 1L);

        assertEquals(OrderStatus.processing, result.getStatus());
        assertEquals("TRACK123", result.getTrackingCode());
        // Verify no stock refund was triggered
        verify(productRepository, never()).findByIdForUpdate(anyLong());
        verify(warehouseLogRepository, never()).save(any(WarehouseLog.class));
    }

    @Test
    @DisplayName("Hủy đơn hàng kích hoạt khóa bi quan hoàn trả tồn kho và tạo WarehouseLog IMPORT")
    void testCancelOrderTriggersPessimisticLockStockRefund() {
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.of(sampleOrder));
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(product1));
        when(productRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = adminOrderService.updateStatus(999L, OrderStatus.cancelled, null, 1L);

        assertEquals(OrderStatus.cancelled, result.getStatus());
        // Stock product1: 10 + 2 = 12
        assertEquals(12, product1.getQuantity());
        // Stock product2: 5 + 1 = 6
        assertEquals(6, product2.getQuantity());

        verify(productRepository).findByIdForUpdate(101L);
        verify(productRepository).findByIdForUpdate(102L);

        ArgumentCaptor<WarehouseLog> logCaptor = ArgumentCaptor.forClass(WarehouseLog.class);
        verify(warehouseLogRepository, times(2)).save(logCaptor.capture());

        List<WarehouseLog> logs = logCaptor.getAllValues();
        assertEquals(2, logs.size());
        assertEquals(WarehouseLogType.IMPORT, logs.get(0).getType());
        assertEquals(2, logs.get(0).getQuantity());
        assertEquals(999L, logs.get(0).getReferenceId());
        assertEquals("order_cancel", logs.get(0).getReason());
    }

    @Test
    @DisplayName("Hủy đơn hàng đã thanh toán (paid) tự động chuyển sang refunded")
    void testCancelPaidOrderTransitionsToRefunded() {
        sampleOrder.setPaymentStatus(PaymentStatus.paid);
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.of(sampleOrder));
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(product1));
        when(productRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = adminOrderService.updateStatus(999L, OrderStatus.cancelled, null, 1L);

        assertEquals(OrderStatus.cancelled, result.getStatus());
        assertEquals(PaymentStatus.refunded, result.getPaymentStatus());
    }

    @Test
    @DisplayName("Chặn không cho phép hủy đơn đã hoàn thành (completed)")
    void testCannotCancelCompletedOrder() {
        sampleOrder.setStatus(OrderStatus.completed);
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.of(sampleOrder));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                adminOrderService.updateStatus(999L, OrderStatus.cancelled, null, 1L));

        assertTrue(ex.getMessage().contains("hoàn thành, không thể hủy trực tiếp"));
        verify(productRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("Chặn không cho phép hủy đơn đang giao (shipped)")
    void testCannotCancelShippedOrder() {
        sampleOrder.setStatus(OrderStatus.shipped);
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.of(sampleOrder));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                adminOrderService.updateStatus(999L, OrderStatus.cancelled, null, 1L));

        assertTrue(ex.getMessage().contains("đang giao, không thể hủy trực tiếp"));
        verify(productRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("Chặn không cho phép đổi trạng thái của đơn hàng đã bị hủy")
    void testCannotChangeStatusOfCancelledOrder() {
        sampleOrder.setStatus(OrderStatus.cancelled);
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.of(sampleOrder));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                adminOrderService.updateStatus(999L, OrderStatus.processing, null, 1L));

        assertTrue(ex.getMessage().contains("đã bị hủy, không thể thay đổi trạng thái"));
    }
}
