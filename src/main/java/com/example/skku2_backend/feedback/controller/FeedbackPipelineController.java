 package com.example.skku2_backend.feedback.controller;

import com.example.skku2_backend.feedback.gpt.FeedbackPipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackPipelineController {

    private final FeedbackPipelineService feedbackPipelineService;

    // 1~5단계를 한번에 실행: DB 조회 -> 상관관계 계산 -> GPT 호출 -> 저장
    @PostMapping("/{userId}/generate")
    public ResponseEntity<Void> generate(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "90") int periodDays
    ) {
        Long feedbackId = feedbackPipelineService.runPipeline(userId, periodDays);
        return ResponseEntity.created(URI.create("/api/feedback/" + feedbackId)).build();
    }
}
