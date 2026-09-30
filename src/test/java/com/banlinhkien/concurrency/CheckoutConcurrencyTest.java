package com.banlinhkien.concurrency;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CartItemDto;
import com.banlinhkien.dto.CheckoutRequestDto;
import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.Product;
import com.banlinhkien.exception.InsufficientStockException;
import com.banlinhkien.repository.OrderItemRepository;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.WarehouseLogRepository;
import com.banlinhkien.service.CheckoutService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class CheckoutConcurrencyTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private WarehouseLogRepository warehouseLogRepository;

    private Product testProduct;
    private final List<Long> createdOrderIds = new CopyOnWriteArrayList<>();

    @AfterEach
    void tearDown() {
        // Clean up created orders, items, logs and product
        for (Long orderId : createdOrderIds) {
            try {
                orderItemRepository.deleteAll(orderItemRepository.findByOrderId(orderId));
                warehouseLogRepository.deleteAll(warehouseLogRepository.findByReferenceIdOrderByCreatedAtDesc(orderId));
                orderRepository.deleteById(orderId);
            } catch (Exception ignored) {
            }
        }
        if (testProduct != null && testProduct.getId() != null) {
            try {
                productRepository.deleteById(testProduct.getId());
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Pessimistic Locking: 10 concurrent threads buying product with stock=2 -> exactly 2 succeed, 8 fail, stock=0")
    void testConcurrentCheckoutAntiOverselling() throws InterruptedException {
        // 1. Arrange: Create a dedicated product with exact quantity = 2
        testProduct = Product.builder()
                .name("Linh kiện Concurrency Test CPU X1")
                .price(new BigDecimal("5000000"))
                .costPrice(new BigDecimal("4500000"))
                .quantity(2)
                .isActive(true)
                .isFeatured(false)
                .build();
        testProduct = productRepository.save(testProduct);
        Long productId = testProduct.getId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Throwable> exceptions = new CopyOnWriteArrayList<>();

        // 2. Act: Dispatch 10 concurrent threads simultaneously
        for (int i = 1; i <= threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    // Block until the start signal is given
                    startSignal.await();

                    // Prepare thread-isolated cart and checkout request
                    CartDto cart = new CartDto();
                    cart.getItems().put(productId, CartItemDto.builder()
                            .productId(productId)
                            .name(testProduct.getName())
                            .price(testProduct.getPrice())
                            .quantity(1)
                            .maxStock(2)
                            .build());

                    CheckoutRequestDto request = CheckoutRequestDto.builder()
                            .customerName("Khách hàng Luồng #" + threadIndex)
                            .customerPhone("098765432" + threadIndex)
                            .customerEmail("concurrency_" + threadIndex + "@test.vn")
                            .customerAddress("Số " + threadIndex + " Phố Công Nghệ")
                            .province("Hà Nội")
                            .paymentMethod("cod")
                            .build();

                    // Call pure Java CheckoutService (direct service execution)
                    Order order = checkoutService.placeOrder(request, cart, null);
                    createdOrderIds.add(order.getId());
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    // Expected when stock is exhausted
                    failureCount.incrementAndGet();
                } catch (Throwable t) {
                    exceptions.add(t);
                    failureCount.incrementAndGet();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        // Trigger all 10 threads at the exact same millisecond
        startSignal.countDown();

        // Wait up to 15 seconds for all threads to complete
        boolean finished = doneSignal.await(15, TimeUnit.SECONDS);
        assertTrue(finished, "All checkout threads should complete within 15 seconds");
        executor.shutdown();

        // 3. Assert: Verify pessimistic locking prevented overselling
        System.out.println("=== CONCURRENCY TEST RESULTS ===");
        System.out.println("Successful orders: " + successCount.get());
        System.out.println("Failed orders: " + failureCount.get());
        if (!exceptions.isEmpty()) {
            System.out.println("Unexpected exceptions: " + exceptions);
        }

        assertEquals(2, successCount.get(), "Exactly 2 orders must succeed for stock of 2");
        assertEquals(8, failureCount.get(), "Remaining 8 orders must fail due to InsufficientStockException");

        // Reload fresh product entity from MySQL
        Product finalProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(0, finalProduct.getQuantity(), "Final product stock in database must be exactly 0 (never negative!)");
    }
}
