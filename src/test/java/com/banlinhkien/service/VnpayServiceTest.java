package com.banlinhkien.service;

import com.banlinhkien.config.VnpayProperties;
import com.banlinhkien.dto.VnpayResultDto;
import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.OrderStatusHistory;
import com.banlinhkien.entity.PaymentTransaction;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.enums.PaymentMethod;
import com.banlinhkien.enums.PaymentStatus;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.OrderStatusHistoryRepository;
import com.banlinhkien.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class VnpayServiceTest {

    @Autowired
    private VnpayService vnpayService;

    @Autowired
    private VnpayProperties vnpayProperties;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .customerName("Khách Hàng VNPay Test")
                .customerPhone("0912345678")
                .customerEmail("vnpay_test@banlinhkien.vn")
                .customerAddress("123 Phố Huế, Hai Bà Trưng")
                .shippingProvince("Hà Nội")
                .totalAmount(new BigDecimal("2500000"))
                .shippingFee(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .paymentMethod(PaymentMethod.vnpay)
                .paymentStatus(PaymentStatus.unpaid)
                .status(OrderStatus.pending)
                .createdAt(LocalDateTime.now())
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        if (testOrder != null && testOrder.getId() != null) {
            paymentTransactionRepository.deleteAll(paymentTransactionRepository.findByOrderId(testOrder.getId()));
            orderStatusHistoryRepository.deleteAll(orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(testOrder.getId()));
            orderRepository.deleteById(testOrder.getId());
        }
    }

    @Test
    @DisplayName("Should generate valid signed VNPay payment URL with HMAC-SHA512")
    void testCreatePaymentUrl() {
        String paymentUrl = vnpayService.createPaymentUrl(testOrder, "127.0.0.1");

        assertNotNull(paymentUrl);
        assertTrue(paymentUrl.startsWith(vnpayProperties.getUrl()));
        assertTrue(paymentUrl.contains("vnp_Amount=250000000")); // 2,500,000 * 100
        assertTrue(paymentUrl.contains("vnp_TmnCode=" + vnpayProperties.getTmnCode()));
        assertTrue(paymentUrl.contains("vnp_TxnRef=" + testOrder.getId() + "_"));
        assertTrue(paymentUrl.contains("vnp_SecureHash="));
    }

    @Test
    @DisplayName("Should verify valid HMAC-SHA512 checksum")
    void testVerifyChecksumValid() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "250000000");
        params.put("vnp_Command", "pay");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");

        // Calculate valid hash
        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        assertTrue(vnpayService.verifyChecksum(params));
    }

    @Test
    @DisplayName("Should reject invalid or tampered HMAC-SHA512 checksum")
    void testVerifyChecksumInvalid() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "250000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_SecureHash", "tampered_fake_signature_12345");

        assertFalse(vnpayService.verifyChecksum(params));
    }

    @Test
    @DisplayName("Should parse order ID from txnRef")
    void testParseOrderId() {
        assertEquals(105L, vnpayService.parseOrderId("105_1725888000"));
        assertEquals(999L, vnpayService.parseOrderId("999"));
        assertNull(vnpayService.parseOrderId("invalid_ref"));
        assertNull(vnpayService.parseOrderId(null));
    }

    @Test
    @DisplayName("Should successfully process valid VNPay payment: state transition to PROCESSING & PAID, create transaction & history logs")
    void testProcessPaymentResultSuccess() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "250000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VNPAY1489201");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        // 1. Process first time
        VnpayResultDto result = vnpayService.processPaymentResult(params);

        assertTrue(result.isSuccess());
        assertEquals("00", result.getCode());

        // Verify order in database
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(PaymentStatus.paid, updatedOrder.getPaymentStatus());
        assertEquals(OrderStatus.processing, updatedOrder.getStatus());
        assertEquals("VNPAY1489201", updatedOrder.getVnpayTransaction());

        // Verify payment transaction created
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderId(testOrder.getId());
        assertEquals(1, transactions.size());
        assertEquals("success", transactions.get(0).getStatus());
        assertEquals("vnpay", transactions.get(0).getGateway());

        // Verify status history created
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(testOrder.getId());
        assertEquals(1, histories.size());
        assertEquals("processing", histories.get(0).getToStatus());

        // 2. Idempotency test: Call again with same params -> should return RspCode 02 (Already confirmed)
        VnpayResultDto secondResult = vnpayService.processPaymentResult(params);
        assertTrue(secondResult.isSuccess());
        assertEquals("02", secondResult.getCode());

        // Still exactly 1 transaction and 1 history record (no duplication!)
        assertEquals(1, paymentTransactionRepository.findByOrderId(testOrder.getId()).size());
        assertEquals(1, orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(testOrder.getId()).size());
    }

    @Test
    @DisplayName("Should reject payment callback with tampered amount (RspCode 04)")
    void testProcessPaymentResultAmountMismatch() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "100000000"); // 1,000,000 VND instead of 2,500,000 VND
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VNPAY1489201");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        VnpayResultDto result = vnpayService.processPaymentResult(params);

        assertFalse(result.isSuccess());
        assertEquals("04", result.getCode());

        // Order remains unpaid
        Order order = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(PaymentStatus.unpaid, order.getPaymentStatus());
    }

    private String computeTestHash(Map<String, String> params, String secret) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (String f : fieldNames) {
            if (sb.length() > 0) sb.append('&');
            sb.append(URLEncoder.encode(f, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(params.get(f), StandardCharsets.US_ASCII));
        }

        return VnpayService.hmacSHA512(secret, sb.toString());
    }
}
