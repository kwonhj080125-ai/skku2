package com.example.skku2_backend.feedback.gpt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GPT가 response_format=json_object로 반환하는 JSON을 그대로 매핑하는 DTO.
 * { headline, supporting_stat, confidence, secondary_patterns, insufficient_data_note }
 */
@Getter
@NoArgsConstructor
public class GptFeedbackResult {

    private String headline;

    @JsonProperty("supporting_stat")
    private String supportingStat;

    private String confidence; // "high" | "medium" | "low"

    @JsonProperty("secondary_patterns")
    private List<String> secondaryPatterns;

    @JsonProperty("insufficient_data_note")
    private String insufficientDataNote;
}
