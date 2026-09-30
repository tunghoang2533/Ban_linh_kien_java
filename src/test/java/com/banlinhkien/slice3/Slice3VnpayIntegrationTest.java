package com.banlinhkien.slice3;

import com.banlinhkien.config.VnpayProperties;
import com.banlinhkien.entity.Order;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.enums.PaymentMethod;
import com.banlinhkien.enums.PaymentStatus;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.OrderStatusHistoryRepository;
import com.banlinhkien.repository.PaymentTransactionRepository;
import com.banlinhkien.service.VnpayService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class Slice3VnpayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
                .customerName("Nguyễn Văn A")
                .customerPhone("0987654321")
                .customerEmail("nguyenvana@gmail.com")
                .customerAddress("456 Lê Duẩn, Hoàn Kiếm")
                .shippingProvince("Hà Nội")
                .totalAmount(new BigDecimal("1500000"))
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
    @DisplayName("Endpoint /vnpay/payment/{orderId} should redirect to VNPay Sandbox with valid signature")
    void testInitiatePaymentRedirect() throws Exception {
        mockMvc.perform(get("/vnpay/payment/" + testOrder.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("sandbox.vnpayment.vn/paymentv2/vpcpay.html")))
                .andExpect(header().string("Location", containsString("vnp_Amount=150000000")))
                .andExpect(header().string("Location", containsString("vnp_TmnCode=" + vnpayProperties.getTmnCode())))
                .andExpect(header().string("Location", containsString("vnp_SecureHash=")));
    }

    @Test
    @DisplayName("Webhook /api/vnpay/ipn: Valid signature returns RspCode 00 and updates Order to PAID")
    void testIpnWebhookSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "150000000"); // 1,500,000 * 100
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VNPAY9876543");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        var requestBuilder = get("/api/vnpay/ipn");
        params.forEach(requestBuilder::param);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));

        // Verify DB update
        Order updated = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(PaymentStatus.paid, updated.getPaymentStatus());
        assertEquals(OrderStatus.processing, updated.getStatus());

        // Test Idempotency on second call
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("02"))
                .andExpect(jsonPath("$.Message").value("Order Already Confirmed"));
    }

    @Test
    @DisplayName("Webhook /api/vnpay/ipn: Invalid signature returns RspCode 97")
    void testIpnWebhookInvalidChecksum() throws Exception {
        mockMvc.perform(get("/api/vnpay/ipn")
                        .param("vnp_Amount", "150000000")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", testOrder.getId() + "_1725888000")
                        .param("vnp_SecureHash", "invalid_fake_checksum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97"))
                .andExpect(jsonPath("$.Message").value("Invalid Checksum"));
    }

    @Test
    @DisplayName("Webhook /api/vnpay/ipn: Amount mismatch returns RspCode 04")
    void testIpnWebhookAmountMismatch() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "50000000"); // 500,000 VND instead of 1,500,000 VND
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VNPAY9876543");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        var requestBuilder = get("/api/vnpay/ipn");
        params.forEach(requestBuilder::param);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("04"))
                .andExpect(jsonPath("$.Message").value("Invalid Amount"));
    }

    @Test
    @DisplayName("Return URL /vnpay/return: Redirects customer to order success page on successful payment")
    void testReturnUrlSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "150000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "VNPAY9876543");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        var requestBuilder = get("/vnpay/return");
        params.forEach(requestBuilder::param);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dat-hang-thanh-cong/" + testOrder.getId() + "?payment=success"));
    }

    @Test
    @DisplayName("Return URL /vnpay/return: Redirects customer to order success page with payment=failed when user cancels")
    void testReturnUrlFailed() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "150000000");
        params.put("vnp_ResponseCode", "24"); // User cancelled
        params.put("vnp_TransactionStatus", "02");
        params.put("vnp_TransactionNo", "0");
        params.put("vnp_TxnRef", testOrder.getId() + "_1725888000");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());

        String hash = computeTestHash(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", hash);

        var requestBuilder = get("/vnpay/return");
        params.forEach(requestBuilder::param);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dat-hang-thanh-cong/" + testOrder.getId() + "?payment=failed"))
                .andExpect(flash().attributeExists("vnpayError"));
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
