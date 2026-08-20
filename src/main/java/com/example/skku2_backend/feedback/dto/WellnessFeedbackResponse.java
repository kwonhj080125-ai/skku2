package com.example.skku2_backend.feedback.dto;

import com.example.skku2_backend.feedback.domain.FeedbackConfidence;
import com.example.skku2_backend.feedback.domain.WellnessFeedback;
import lombok.Getter;

import java.util.List;

@Getter
public class WellnessFeedbackResponse {

    private final Long feedbackId;
    private final Long userId;
    private final String feedbackDate;
    private final String headline;
    private final String supportingStat;
    private final FeedbackConfidence confidence;
    private final List<String> secondaryPatterns;
    private final String insufficientDataNote;

    public WellnessFeedbackResponse(WellnessFeedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.userId = feedback.getUserId();
        this.feedbackDate = feedback.getFeedbackDate().toString();
        this.headline = feedback.getHeadline();
        this.supportingStat = feedback.getSupportingStat();
        this.confidence = feedback.getConfidence();
        this.secondaryPatterns = feedback.getSecondaryPatterns();
        this.insufficientDataNote = feedback.getInsufficientDataNote();
    }
}
