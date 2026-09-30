package com.banlinhkien.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AiRecommendationResponseDto {

    private boolean success;
    private String reply;
    private List<String> suggestions;

    @JsonProperty("build_suggestion")
    private List<Map<String, Object>> buildSuggestion;
}
