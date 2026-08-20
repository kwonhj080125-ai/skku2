package com.example.skku2_backend.feedback.repository;

import com.example.skku2_backend.feedback.domain.WellnessFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WellnessFeedbackRepository extends JpaRepository<WellnessFeedback, Long> {

    // 오늘자 캐시가 이미 있는지 확인 → 있으면 GPT를 다시 호출하지 않고 이 값을 그대로 앱에 내려주면 됨
    Optional<WellnessFeedback> findByUserIdAndFeedbackDate(Long userId, LocalDate feedbackDate);

    // 앱 UI에서 "오늘의 피드백" 하나만 보여줄 때 가장 최근 날짜 기준으로 조회
    Optional<WellnessFeedback> findTopByUserIdOrderByFeedbackDateDesc(Long userId);
}
