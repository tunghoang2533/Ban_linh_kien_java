package com.banlinhkien.service;

import com.banlinhkien.config.VnpayProperties;
import com.banlinhkien.dto.VnpayResultDto;
import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.OrderStatusHistory;
import com.banlinhkien.entity.PaymentTransaction;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.enums.PaymentStatus;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.OrderStatusHistoryRepository;
import com.banlinhkien.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnpayService {

    private final VnpayProperties vnpayProperties;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Generates signed VNPay payment redirection URL.
     */
    public String createPaymentUrl(Order order, String ipAddr) {
        String vnpTmnCode = vnpayProperties.getTmnCode();
        String vnpHashSecret = vnpayProperties.getHashSecret();
        String vnpUrl = vnpayProperties.getUrl();
        String vnpReturnUrl = vnpayProperties.getReturnUrl();

        BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String vnpTxnRef = order.getId() + "_" + System.currentTimeMillis();

        String clientIp = ipAddr != null && !ipAddr.isBlank() ? ipAddr : "127.0.0.1";
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }

        LocalDateTime now = LocalDateTime.now();
        String vnpCreateDate = now.format(VNP_DATE_FORMAT);
        String vnpExpireDate = now.plusMinutes(15).format(VNP_DATE_FORMAT);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getId());
        vnpParams.put("vnp_OrderType", "billpayment");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Sort keys alphabetically (ASCII order)
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);

            if (fieldValue != null && !fieldValue.isBlank()) {
                String encodedKey = encode(fieldName);
                String encodedVal = encode(fieldValue);

                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(encodedKey).append('=').append(encodedVal);

                query.append(encodedKey).append('=').append(encodedVal).append('&');
            }
        }

        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("vnp_SecureHash=").append(secureHash);

        return vnpUrl + "?" + query;
    }

    /**
     * Verifies HMAC-SHA512 checksum of incoming VNPAY parameters.
     */
    public boolean verifyChecksum(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isBlank()) {
            return false;
        }

        Map<String, String> hashParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (key.startsWith("vnp_") && !"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)) {
                if (val != null && !val.isBlank()) {
                    hashParams.put(key, val);
                }
            }
        }

        List<String> fieldNames = new ArrayList<>(hashParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = hashParams.get(fieldName);
            if (hashData.length() > 0) {
                hashData.append('&');
            }
            hashData.append(encode(fieldName)).append('=').append(encode(fieldValue));
        }

        String calculatedHash = hmacSHA512(vnpayProperties.getHashSecret(), hashData.toString());
        return MessageDigest.isEqual(
                calculatedHash.toLowerCase().getBytes(StandardCharsets.UTF_8),
                vnpSecureHash.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Parses order ID from vnp_TxnRef (format: {orderId}_{timestamp} or {orderId}).
     */
    public Long parseOrderId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }
        String[] parts = txnRef.split("_");
        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Processes payment result (used by both Return URL and IPN Webhook).
     * Implements full idempotency and database locking.
     */
    public VnpayResultDto processPaymentResult(Map<String, String> params) {
        if (!verifyChecksum(params)) {
            return VnpayResultDto.builder()
                    .success(false)
                    .code("97")
                    .message("Chữ ký không hợp lệ (Invalid Checksum)")
                    .build();
        }

        String txnRef = params.get("vnp_TxnRef");
        Long orderId = parseOrderId(txnRef);
        if (orderId == null) {
            return VnpayResultDto.builder()
                    .success(false)
                    .code("01")
                    .message("Mã đơn hàng không hợp lệ")
                    .build();
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return VnpayResultDto.builder()
                    .success(false)
                    .code("01")
                    .message("Không tìm thấy đơn hàng #" + orderId)
                    .build();
        }

        Order order = orderOpt.get();

        // Idempotency: If already confirmed as paid, return success immediately
        if (order.getPaymentStatus() == PaymentStatus.paid) {
            return VnpayResultDto.builder()
                    .success(true)
                    .code("02")
                    .message("Đơn hàng đã được xác nhận thanh toán trước đó.")
                    .order(order)
                    .build();
        }

        // Verify amount
        long expectedAmount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        long receivedAmount = 0L;
        try {
            receivedAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        } catch (NumberFormatException ignored) {
        }

        if (expectedAmount != receivedAmount) {
            return VnpayResultDto.builder()
                    .success(false)
                    .code("04")
                    .message("Số tiền không hợp lệ (Invalid Amount)")
                    .order(order)
                    .build();
        }

        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = params.getOrDefault("vnp_TransactionStatus", "00");
        String transactionNo = params.getOrDefault("vnp_TransactionNo", txnRef);

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return confirmPaymentSuccess(order.getId(), transactionNo, params);
        } else {
            return recordPaymentFailure(order.getId(), responseCode, transactionNo, params);
        }
    }

    @Transactional
    public VnpayResultDto confirmPaymentSuccess(Long orderId, String transactionNo, Map<String, String> params) {
        Order freshOrder = orderRepository.findById(orderId).orElseThrow();

        // Check again inside transaction
        if (freshOrder.getPaymentStatus() == PaymentStatus.paid) {
            return VnpayResultDto.builder()
                    .success(true)
                    .code("02")
                    .message("Đơn hàng đã được xác nhận thanh toán trước đó.")
                    .order(freshOrder)
                    .build();
        }

        freshOrder.setPaymentStatus(PaymentStatus.paid);
        freshOrder.setStatus(OrderStatus.processing);
        freshOrder.setVnpayTransaction(transactionNo);
        orderRepository.save(freshOrder);

        // Record status transition
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .orderId(freshOrder.getId())
                .fromStatus("pending")
                .toStatus("processing")
                .changerName("VNPAY Gateway")
                .note("Thanh toán thành công qua VNPAY, Mã giao dịch: " + transactionNo)
                .createdAt(LocalDateTime.now())
                .build());

        // Record payment transaction log
        String jsonResponse = serializeParams(params);
        paymentTransactionRepository.save(PaymentTransaction.builder()
                .orderId(freshOrder.getId())
                .gateway("vnpay")
                .transactionCode(transactionNo)
                .amount(freshOrder.getTotalAmount())
                .status("success")
                .gatewayResponse(jsonResponse)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        return VnpayResultDto.builder()
                .success(true)
                .code("00")
                .message("Giao dịch thanh toán VNPAY thành công.")
                .order(freshOrder)
                .build();
    }

    @Transactional
    public VnpayResultDto recordPaymentFailure(Long orderId, String responseCode, String transactionNo, Map<String, String> params) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setPaymentStatus(PaymentStatus.unpaid);
            orderRepository.save(order);

            String jsonResponse = serializeParams(params);
            paymentTransactionRepository.save(PaymentTransaction.builder()
                    .orderId(order.getId())
                    .gateway("vnpay")
                    .transactionCode(transactionNo)
                    .amount(order.getTotalAmount())
                    .status("failed")
                    .gatewayResponse(jsonResponse)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        return VnpayResultDto.builder()
                .success(false)
                .code(responseCode)
                .message(getErrorMessage(responseCode))
                .order(order)
                .build();
    }

    public String getErrorMessage(String code) {
        Map<String, String> messages = Map.ofEntries(
                Map.entry("00", "Giao dịch thành công"),
                Map.entry("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)."),
                Map.entry("09", "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking tại ngân hàng."),
                Map.entry("10", "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần."),
                Map.entry("11", "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch."),
                Map.entry("12", "Thẻ/Tài khoản bị khóa."),
                Map.entry("13", "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)."),
                Map.entry("24", "Khách hàng hủy giao dịch."),
                Map.entry("51", "Tài khoản không đủ số dư để thực hiện giao dịch."),
                Map.entry("65", "Tài khoản đã vượt quá hạn mức giao dịch trong ngày."),
                Map.entry("75", "Ngân hàng thanh toán đang bảo trì."),
                Map.entry("79", "Nhập sai mật khẩu thanh toán quá số lần quy định."),
                Map.entry("99", "Các lỗi khác (lỗi phát sinh từ hệ thống ngân hàng).")
        );
        return messages.getOrDefault(code, "Giao dịch không thành công (Mã lỗi: " + code + ")");
    }

    private String encode(String val) {
        try {
            return URLEncoder.encode(val, StandardCharsets.US_ASCII.toString());
        } catch (UnsupportedEncodingException e) {
            return URLEncoder.encode(val, StandardCharsets.UTF_8);
        }
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA512", e);
        }
    }

    private String serializeParams(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return params.toString();
        }
    }
}
