package com.example.skku2_backend.feedback.gpt;

import com.example.skku2_backend.dailyrecord.dto.CorrelationResult;
import com.example.skku2_backend.feedback.domain.FeedbackConfidence;
import com.example.skku2_backend.feedback.dto.WellnessFeedbackCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 파이프라인 3~4단계를 묶은 서비스: 프롬프트 조립 -> GPT 호출 -> 결과 검증.
 * 여기서 반환하는 WellnessFeedbackCreateRequest를 그대로
 * WellnessFeedbackService.save()에 넘기면 5단계(저장)까지 이어진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptFeedbackGenerationService {

    private final GptPromptBuilder gptPromptBuilder;
    private final GptClient gptClient;

    public WellnessFeedbackCreateRequest generate(
            Long userId, LocalDate feedbackDate, int periodDays, List<CorrelationResult> correlationData
    ) {
        String userPrompt = gptPromptBuilder.buildUserPrompt(userId, periodDays, correlationData);
        GptFeedbackResult result = gptClient.chat(GptPromptBuilder.SYSTEM_PROMPT, userPrompt);

        validateAgainstSource(result, correlationData);

        return WellnessFeedbackCreateRequest.builder()
                .userId(userId)
                .feedbackDate(feedbackDate)
                .headline(result.getHeadline())
                .supportingStat(result.getSupportingStat())
                .confidence(parseConfidence(result.getConfidence()))
                .secondaryPatterns(result.getSecondaryPatterns())
                .insufficientDataNote(result.getInsufficientDataNote())
                .build();
    }

    /** GPT가 System Prompt 규칙(숫자 창작 금지)을 어기고 원본에 없는 통계를 만들어냈는지 가벼운 가드 */
    private void validateAgainstSource(GptFeedbackResult result, List<CorrelationResult> correlationData) {
        if (result.getSupportingStat() == null || correlationData.isEmpty()) {
            return;
        }
        boolean matchesKnownStat = correlationData.stream()
                .anyMatch(r -> result.getSupportingStat().equals(r.getOccurrence() + "번 중 " + r.getTotal() + "번"));

        if (!matchesKnownStat) {
            log.warn("[GPT 검증] 원본 데이터에 없는 통계 문구 감지: '{}'", result.getSupportingStat());
        }
    }

    private FeedbackConfidence parseConfidence(String raw) {
        if (raw == null) return FeedbackConfidence.LOW;
        try {
            return FeedbackConfidence.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[GPT 검증] confidence 값 파싱 실패, LOW로 대체: '{}'", raw);
            return FeedbackConfidence.LOW;
        }
    }
}
