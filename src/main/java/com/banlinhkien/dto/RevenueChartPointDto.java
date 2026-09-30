package com.banlinhkien.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueChartPointDto {

    private String monthLabel;
    private BigDecimal revenue;
    private long orderCount;
}
