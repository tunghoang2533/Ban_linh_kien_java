package com.banlinhkien.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendationRequestDto {

    private String message;
    private BigDecimal budget;
    private String purpose;
    private List<Map<String, String>> history;
}
