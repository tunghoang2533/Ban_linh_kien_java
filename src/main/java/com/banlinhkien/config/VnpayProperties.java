package com.banlinhkien.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VnpayProperties {
    private String tmnCode = "YOUR_TMN_CODE";
    private String hashSecret = "TEST_SECRET_KEY";
    private String url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:8080/vnpay/return";
}
