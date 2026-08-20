package com.example.skku2_backend.dailyrecord.service;

import com.example.skku2_backend.dailyrecord.domain.DailyWellnessRecord;
import com.example.skku2_backend.dailyrecord.dto.CorrelationResult;
import com.example.skku2_backend.dailyrecord.dto.DailyWellnessRecordCreateRequest;
import com.example.skku2_backend.dailyrecord.repository.DailyWellnessRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyWellnessRecordService {

    private final DailyWellnessRecordRepository dailyWellnessRecordRepository;
    private final CorrelationAnalysisService correlationAnalysisService;

    /**
     * 같은 유저+같은 날짜 기록이 이미 있으면 덮어쓰고(update), 없으면 새로 만든다(insert).
     * 하루 여러 번 기록을 보내는 경우(오전엔 체중만, 저녁엔 수면 데이터만 오는 식)에도
     * 하나의 레코드로 계속 누적되도록 하는 게 목적.
     */
    @Transactional
    public Long save(DailyWellnessRecordCreateRequest request) {
        DailyWellnessRecord existing = dailyWellnessRecordRepository
                .findByUserIdAndLoggedDate(request.getUserId(), request.getLoggedDate())
                .orElse(null);

        DailyWellnessRecord record = DailyWellnessRecord.createOrUpdate(
                existing,
                request.getUserId(),
                request.getLoggedDate(),
                request.getDailySkinScore(),
                request.getSkinAge(),
                request.getOilyIntensityScore(),
                request.getBlackheadScore(),
                request.getPoresScore(),
                request.getMorningWeightKg(),
                request.getEveningWeightKg(),
                request.getBodyWaterMassKg(),
                request.getSkeletalMuscleMassKg(),
                request.getBodyFatMassKg(),
                request.getSubcutaneousFatMm(),
                request.getBodyFatPercentage(),
                request.getSleepDurationMinutes(),
                request.getBedtime(),
                request.getWakeTime(),
                request.getWaterIntakeMl(),
                request.getIsFasting(),
                request.getIsRestDay(),
                request.getTotalCaloriesKcal(),
                request.getFatG(),
                request.getSugarG(),
                request.getSodiumMg(),
                request.getDeepSleepRatio(),
                request.getMenstrualPhase(),
                request.getUvIndex(),
                request.getPm25Value()
        );

        return dailyWellnessRecordRepository.save(record).getRecordId();
    }

    /**
     * 파이프라인 1~2단계: 최근 days일치 기록을 DB에서 List로 조회한 뒤 상관관계를 계산한다.
     * 이 결과가 GPT 프롬프트에 그대로 들어가는 재료가 된다.
     */
    public List<CorrelationResult> getCorrelations(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);

        List<DailyWellnessRecord> records = dailyWellnessRecordRepository
                .findByUserIdAndLoggedDateBetweenOrderByLoggedDateAsc(userId, start, end);

        return correlationAnalysisService.analyze(records);
    }
}
