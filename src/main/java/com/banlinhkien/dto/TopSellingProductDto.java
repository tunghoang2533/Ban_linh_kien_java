package com.banlinhkien.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductDto {
    private String productName;
    private Long totalSold;
}
