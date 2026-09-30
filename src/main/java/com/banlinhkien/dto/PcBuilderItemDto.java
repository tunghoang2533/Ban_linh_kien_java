package com.banlinhkien.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcBuilderItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private String image;
    private Integer quantity;
    private boolean inStock;
    private String socket;
}
