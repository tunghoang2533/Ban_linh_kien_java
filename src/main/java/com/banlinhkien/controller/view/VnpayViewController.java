package com.banlinhkien.controller.view;

import com.banlinhkien.dto.VnpayResultDto;
import com.banlinhkien.entity.Order;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VnpayViewController {

    private final VnpayService vnpayService;
    private final OrderRepository orderRepository;

    /** Accepts only IPv4 addresses to prevent header injection. */
    private static final Pattern IP_PATTERN =
            Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");

    /**
     * Initiates VNPay payment for an order.
     * Requires a valid access token to prevent IDOR — an attacker cannot
     * create a payment URL for another user's order by guessing the orderId.
     */
    @GetMapping("/payment/{orderId}")
    public String initiatePayment(
            @PathVariable("orderId") Long orderId,
            @RequestParam(name = "token", required = false) String token,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));

        if (token == null || !token.equals(order.getAccessToken())) {
            redirectAttributes.addFlashAttribute("error", "Liên kết thanh toán không hợp lệ.");
            return "redirect:/";
        }

        String ipAddr = getClientIp(request);
        String paymentUrl = vnpayService.createPaymentUrl(order, ipAddr);
        log.info("Redirecting order #{} to VNPay payment URL", orderId);
        return "redirect:" + paymentUrl;
    }

    @GetMapping("/return")
    public String handleReturn(
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {

        log.info("Received VNPay return callback with params: {}", params);
        VnpayResultDto result = vnpayService.processPaymentResult(params);

        if (result.getOrder() != null) {
            Long orderId = result.getOrder().getId();
            String token = result.getOrder().getAccessToken();
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("vnpaySuccess", result.getMessage());
                return "redirect:/dat-hang-thanh-cong/" + orderId
                        + "?token=" + token + "&payment=success";
            } else {
                redirectAttributes.addFlashAttribute("vnpayError", result.getMessage());
                return "redirect:/dat-hang-thanh-cong/" + orderId
                        + "?token=" + token + "&payment=failed";
            }
        }

        redirectAttributes.addFlashAttribute("error", result.getMessage());
        return "redirect:/";
    }

    /**
     * Safely extracts the client IP address.
     * Takes the first IP in X-Forwarded-For (set by reverse proxy) and
     * validates it is a proper IPv4 address to prevent header-injection attacks.
     * Falls back to the direct remote address if the header is absent or invalid.
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            String firstIp = xfHeader.split(",")[0].trim();
            if (IP_PATTERN.matcher(firstIp).matches()) {
                return firstIp;
            }
        }
        return request.getRemoteAddr();
    }
}
