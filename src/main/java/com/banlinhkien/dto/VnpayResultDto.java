package com.banlinhkien.dto;

import com.banlinhkien.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnpayResultDto {
    private boolean success;
    private String code;
    private String message;
    private Order order;
}
