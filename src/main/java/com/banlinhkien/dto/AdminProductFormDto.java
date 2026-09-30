package com.banlinhkien.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductFormDto {

    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal discountPercent;
    private Integer quantity;
    private Integer minStock;
    private Boolean isActive;
    private Boolean isFeatured;
    private String description;
    private String image;

    @Builder.Default
    private List<String> specNames = new ArrayList<>();

    @Builder.Default
    private List<String> specValues = new ArrayList<>();
}
