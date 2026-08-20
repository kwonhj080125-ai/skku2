package com.example.skku2_backend.feedback.controller;

import com.example.skku2_backend.feedback.dto.WellnessFeedbackCreateRequest;
import com.example.skku2_backend.feedback.dto.WellnessFeedbackResponse;
import com.example.skku2_backend.feedback.service.WellnessFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class WellnessFeedbackController {

    private final WellnessFeedbackService wellnessFeedbackService;

    // 백엔드 배치/파이프라인이 GPT 응답을 받은 직후 호출 (5단계)
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid WellnessFeedbackCreateRequest request) {
        Long feedbackId = wellnessFeedbackService.save(request);
        return ResponseEntity.created(URI.create("/api/feedback/" + feedbackId)).build();
    }

    // 앱이 "오늘의 피드백" 카드를 그릴 때 호출 (6단계)
    @GetMapping("/{userId}/latest")
    public ResponseEntity<WellnessFeedbackResponse> getLatest(@PathVariable Long userId) {
        return ResponseEntity.ok(wellnessFeedbackService.getLatest(userId));
    }
}
