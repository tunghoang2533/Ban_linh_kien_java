package com.banlinhkien.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductQuickViewDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private BigDecimal discountPercent;
    private boolean hasDiscount;
    private Integer quantity;
    private String image;
    private String categoryName;
    private String brandName;
    private String description;
}
