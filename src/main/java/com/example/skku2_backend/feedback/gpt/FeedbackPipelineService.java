package com.example.skku2_backend.feedback.gpt;

import com.example.skku2_backend.dailyrecord.dto.CorrelationResult;
import com.example.skku2_backend.dailyrecord.service.DailyWellnessRecordService;
import com.example.skku2_backend.feedback.dto.WellnessFeedbackCreateRequest;
import com.example.skku2_backend.feedback.service.WellnessFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 파이프라인 1~5단계를 전부 이어서 실행한다.
 * 1~2단계(DailyWellnessRecordService.getCorrelations)는 이미 만들어져 있으므로 여기서는
 * 그 결과를 3~4단계(GptFeedbackGenerationService)에 넘기고, 최종 결과를 5단계(WellnessFeedbackService)로 저장한다.
 */
@Service
@RequiredArgsConstructor
public class FeedbackPipelineService {

    private final DailyWellnessRecordService dailyWellnessRecordService;
    private final GptFeedbackGenerationService gptFeedbackGenerationService;
    private final WellnessFeedbackService wellnessFeedbackService;

    public Long runPipeline(Long userId, int periodDays) {
        // 1~2단계: DB 조회 + 백엔드 상관관계 계산
        List<CorrelationResult> correlationData = dailyWellnessRecordService.getCorrelations(userId, periodDays);

        // 3~4단계: 프롬프트 조립 + GPT API 호출
        LocalDate feedbackDate = LocalDate.now();
        WellnessFeedbackCreateRequest feedbackRequest = gptFeedbackGenerationService.generate(
                userId, feedbackDate, periodDays, correlationData
        );

        // 5단계: 응답 검증(이미 완료) 및 DB 저장
        return wellnessFeedbackService.save(feedbackRequest);
    }
}
