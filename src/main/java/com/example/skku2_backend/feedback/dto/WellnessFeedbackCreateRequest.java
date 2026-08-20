package com.example.skku2_backend.feedback.dto;

import com.example.skku2_backend.feedback.domain.FeedbackConfidence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * GPT 응답 JSON 스키마와 1:1로 매핑되는 저장 요청 DTO.
 * { headline, supporting_stat, confidence, secondary_patterns, insufficient_data_note }
 *
 * 클라이언트가 JSON으로 보낼 때는 @NoArgsConstructor + Jackson이 채워주고,
 * 백엔드 파이프라인(GptFeedbackGenerationService)이 코드로 직접 만들 때는 @Builder를 쓴다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellnessFeedbackCreateRequest {

    @NotNull(message = "userId는 필수입니다.")
    private Long userId;

    @NotNull(message = "feedbackDate는 필수입니다.")
    private LocalDate feedbackDate;

    @NotBlank(message = "headline은 필수입니다.")
    private String headline;

    private String supportingStat;

    @NotNull(message = "confidence는 필수입니다.")
    private FeedbackConfidence confidence;

    private List<String> secondaryPatterns;

    private String insufficientDataNote;
}
