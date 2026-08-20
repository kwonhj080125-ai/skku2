package com.example.skku2_backend.dailyrecord.controller;

import com.example.skku2_backend.dailyrecord.dto.CorrelationResult;
import com.example.skku2_backend.dailyrecord.dto.DailyWellnessRecordCreateRequest;
import com.example.skku2_backend.dailyrecord.service.DailyWellnessRecordService;
import com.example.skku2_backend.dailyrecord.service.DummyDataSeederService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-records")
@RequiredArgsConstructor
public class DailyWellnessRecordController {

    private final DailyWellnessRecordService dailyWellnessRecordService;
    private final DummyDataSeederService dummyDataSeederService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid DailyWellnessRecordCreateRequest request) {
        Long recordId = dailyWellnessRecordService.save(request);
        return ResponseEntity.created(URI.create("/api/daily-records/" + recordId)).build();
    }

    // 파이프라인 1~2단계: 최근 N일 기록을 조회해서 상관관계 계산 결과를 반환.
    // 이 응답을 그대로 GPT 프롬프트에 넣으면 됨 (3~4단계로 이어짐).
    @GetMapping("/{userId}/correlations")
    public ResponseEntity<List<CorrelationResult>> getCorrelations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "90") int days
    ) {
        return ResponseEntity.ok(dailyWellnessRecordService.getCorrelations(userId, days));
    }

    // 개발/테스트 전용: 더미 데이터를 한번에 N일치 생성. 운영 배포 전엔 반드시 제거하거나 막을 것.
    @PostMapping("/{userId}/seed")
    public ResponseEntity<Map<String, Integer>> seed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "120") int days
    ) {
        int savedCount = dummyDataSeederService.seed(userId, days);
        return ResponseEntity.ok(Map.of("savedCount", savedCount));
    }
}
