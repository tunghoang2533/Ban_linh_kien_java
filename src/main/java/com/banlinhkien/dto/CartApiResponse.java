package com.banlinhkien.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartApiResponse {
    private boolean success;
    private String message;

    @JsonProperty("cart_count")
    private int cartCount;

    private BigDecimal subtotal;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    private CartItemDto item;
}
