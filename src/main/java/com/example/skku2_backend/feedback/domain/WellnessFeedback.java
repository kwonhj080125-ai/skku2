package com.example.skku2_backend.feedback.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GPT가 하루치 상관관계 데이터를 문장으로 바꿔준 결과를 캐싱하는 테이블.
 * (파이프라인 5단계 "응답 검증 및 저장"에 해당)
 * 유저당 하루 1건만 존재하도록 유니크 제약을 걸어 매 접속마다 GPT를 다시 부르지 않게 한다.
 */
@Entity
@Table(
        name = "wellness_feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_daily_feedback",
                columnNames = {"user_id", "feedback_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WellnessFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "feedback_date", nullable = false)
    private LocalDate feedbackDate;

    @Lob
    @Column(nullable = false)
    private String headline;

    @Column(name = "supporting_stat")
    private String supportingStat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackConfidence confidence;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "wellness_feedback_secondary_pattern",
            joinColumns = @JoinColumn(name = "feedback_id")
    )
    @Column(name = "pattern")
    private List<String> secondaryPatterns = new ArrayList<>();

    @Column(name = "insufficient_data_note")
    private String insufficientDataNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static WellnessFeedback createOrUpdate(
            WellnessFeedback existing,
            Long userId,
            LocalDate feedbackDate,
            String headline,
            String supportingStat,
            FeedbackConfidence confidence,
            List<String> secondaryPatterns,
            String insufficientDataNote
    ) {
        WellnessFeedback feedback = existing != null ? existing : new WellnessFeedback();
        feedback.userId = userId;
        feedback.feedbackDate = feedbackDate;
        feedback.headline = headline;
        feedback.supportingStat = supportingStat;
        feedback.confidence = confidence;
        feedback.secondaryPatterns = secondaryPatterns != null ? secondaryPatterns : new ArrayList<>();
        feedback.insufficientDataNote = insufficientDataNote;
        return feedback;
    }
}
