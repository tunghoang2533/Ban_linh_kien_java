package com.banlinhkien.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private int quantity;
    private int maxStock;

    public BigDecimal getSubtotal() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
