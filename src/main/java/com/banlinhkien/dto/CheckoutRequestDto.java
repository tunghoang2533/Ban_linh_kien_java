package com.banlinhkien.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequestDto {

    @NotBlank(message = "Vui lòng nhập họ và tên người nhận.")
    private String customerName;

    @NotBlank(message = "Vui lòng nhập số điện thoại người nhận.")
    private String customerPhone;

    private String customerEmail;

    @NotBlank(message = "Vui lòng nhập địa chỉ nhận hàng.")
    private String customerAddress;

    private String province;
    private String district;
    private String ward;

    private String voucherCode;

    @Builder.Default
    private String paymentMethod = "cod";

    private String shippingNote;

    private Boolean saveAddress;
}
