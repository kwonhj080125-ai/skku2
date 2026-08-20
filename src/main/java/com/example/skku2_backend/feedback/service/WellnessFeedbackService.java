package com.example.skku2_backend.feedback.service;

import com.example.skku2_backend.feedback.domain.WellnessFeedback;
import com.example.skku2_backend.feedback.dto.WellnessFeedbackCreateRequest;
import com.example.skku2_backend.feedback.dto.WellnessFeedbackResponse;
import com.example.skku2_backend.feedback.repository.WellnessFeedbackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WellnessFeedbackService {

    private final WellnessFeedbackRepository wellnessFeedbackRepository;

    /**
     * GPT 파이프라인의 5단계(응답 검증 및 저장)에서 호출.
     * 같은 유저+같은 날짜 캐시가 있으면 덮어쓰고, 없으면 새로 만든다.
     * (같은 날 배치가 두 번 도는 경우를 대비한 upsert)
     */
    @Transactional
    public Long save(WellnessFeedbackCreateRequest request) {
        WellnessFeedback existing = wellnessFeedbackRepository
                .findByUserIdAndFeedbackDate(request.getUserId(), request.getFeedbackDate())
                .orElse(null);

        WellnessFeedback feedback = WellnessFeedback.createOrUpdate(
                existing,
                request.getUserId(),
                request.getFeedbackDate(),
                request.getHeadline(),
                request.getSupportingStat(),
                request.getConfidence(),
                request.getSecondaryPatterns(),
                request.getInsufficientDataNote()
        );

        return wellnessFeedbackRepository.save(feedback).getFeedbackId();
    }

    /**
     * 앱 UI(파이프라인 6단계)에서 "오늘의 피드백" 카드를 렌더링할 때 호출.
     * GPT를 다시 부르지 않고 캐싱된 최신 결과만 내려준다.
     */
    public WellnessFeedbackResponse getLatest(Long userId) {
        WellnessFeedback feedback = wellnessFeedbackRepository
                .findTopByUserIdOrderByFeedbackDateDesc(userId)
                .orElseThrow(() -> new EntityNotFoundException("아직 저장된 피드백이 없습니다. userId=" + userId));
        return new WellnessFeedbackResponse(feedback);
    }
}
