package com.banlinhkien.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcComponentDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private Integer quantity;
    private String image;
    private String socket;
}
