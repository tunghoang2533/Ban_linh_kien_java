package com.banlinhkien.controller.api;

import com.banlinhkien.dto.VnpayIpnResponse;
import com.banlinhkien.dto.VnpayResultDto;
import com.banlinhkien.service.VnpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VnpayApiController {

    private final VnpayService vnpayService;

    @GetMapping("/ipn")
    public ResponseEntity<VnpayIpnResponse> handleIpn(@RequestParam Map<String, String> params) {
        log.info("Received VNPay IPN webhook: {}", params);
        VnpayResultDto result = vnpayService.processPaymentResult(params);

        String rspCode = result.getCode();
        String message;

        switch (rspCode) {
            case "97" -> message = "Invalid Checksum";
            case "01" -> message = "Order Not Found";
            case "04" -> message = "Invalid Amount";
            case "02" -> message = "Order Already Confirmed";
            case "00" -> message = "Confirm Success";
            default -> message = result.getMessage();
        }

        return ResponseEntity.ok(VnpayIpnResponse.builder()
                .rspCode(rspCode)
                .message(message)
                .build());
    }
}
