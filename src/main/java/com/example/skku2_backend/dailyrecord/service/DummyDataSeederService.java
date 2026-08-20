package com.example.skku2_backend.dailyrecord.service;

import com.example.skku2_backend.dailyrecord.domain.DailyWellnessRecord;
import com.example.skku2_backend.dailyrecord.domain.MenstrualPhase;
import com.example.skku2_backend.dailyrecord.repository.DailyWellnessRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 테스트/데모용 더미 데이터 생성기.
 * 실제 서비스 로직과는 무관하고, 개발 중 상관관계 분석 엔진이 잘 작동하는지
 * 확인하기 위해 "수면부족 3일 연속 -> 며칠 뒤 피부 악화" 같은 패턴을 일부러 심어서 생성한다.
 *
 * 운영 환경에는 절대 노출하면 안 되는 기능이므로, 나중에 @Profile("dev") 등으로 막아두는 걸 권장.
 */
@Service
@RequiredArgsConstructor
public class DummyDataSeederService {

    private final DailyWellnessRecordRepository dailyWellnessRecordRepository;

    @Transactional
    public int seed(Long userId, int days) {
        Random random = new Random(userId); // userId를 시드로 써서 같은 userId면 매번 같은 데이터가 나오게 함

        // ── 트리거 이벤트를 미리 정해둔다 (몇 번째 날에 무슨 일이 있었는지) ──
        Set<Integer> lowSleepStreakStartDays = new HashSet<>();  // 이 날부터 3일간 수면 부족
        Set<Integer> highSodiumDays = new HashSet<>();
        Set<Integer> lowWaterDays = new HashSet<>();
        for (int i = 5; i < days; i += 18) lowSleepStreakStartDays.add(i);   // 18일마다 한 번, 3일 연속 수면부족
        for (int i = 3; i < days; i += 9) highSodiumDays.add(i);            // 9일마다 나트륨 폭식
        for (int i = 6; i < days; i += 11) lowWaterDays.add(i);             // 11일마다 수분 부족

        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        int savedCount = 0;

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);

            boolean isLowSleepDay = isWithinStreak(lowSleepStreakStartDays, i, 3);
            boolean skinWorsenedByLowSleep = isTriggeredWithLag(lowSleepStreakStartDays, i, 3, 2, 3); // 스트릭 끝난 후 2~3일 뒤
            boolean skinWorsenedBySodium = isTriggeredWithLag(highSodiumDays, i, 1, 1, 2);
            boolean skinWorsenedByLowWater = isTriggeredWithLag(lowWaterDays, i, 1, 1, 2);

            int troubleBump = (skinWorsenedByLowSleep ? 18 : 0);
            int skinScoreDrop = (skinWorsenedByLowSleep ? 12 : 0)
                    + (skinWorsenedBySodium ? 8 : 0)
                    + (skinWorsenedByLowWater ? 5 : 0);

            DailyWellnessRecord record = DailyWellnessRecord.createOrUpdate(
                    null,
                    userId,
                    date,
                    clamp(78 - skinScoreDrop + randInt(random, -4, 4), 30, 100),   // dailySkinScore
                    clamp(29 + randInt(random, -2, 2), 20, 45),                    // skinAge
                    clamp(35 + troubleBump + randInt(random, -5, 5), 10, 100),     // oilyIntensityScore
                    clamp(30 + troubleBump + randInt(random, -5, 5), 10, 100),     // blackheadScore
                    clamp(32 + troubleBump + randInt(random, -5, 5), 10, 100),     // poresScore
                    round1(62.5f + randFloat(random, -0.4f, 0.4f)),                // morningWeightKg
                    round1(63.0f + randFloat(random, -0.4f, 0.4f)),                // eveningWeightKg
                    round1(35.0f + randFloat(random, -0.5f, 0.5f)),                // bodyWaterMassKg
                    round1(24.0f + randFloat(random, -0.3f, 0.3f)),                // skeletalMuscleMassKg
                    round1(15.0f + randFloat(random, -0.5f, 0.5f)),                // bodyFatMassKg
                    round1(1.8f + randFloat(random, -0.2f, 0.2f)),                 // subcutaneousFatMm
                    round1(24.0f + randFloat(random, -1.0f, 1.0f)),                // bodyFatPercentage
                    isLowSleepDay ? randInt(random, 260, 295) : randInt(random, 380, 480), // sleepDurationMinutes
                    LocalDateTime.of(date.minusDays(1), java.time.LocalTime.of(23, 30)),   // bedtime
                    LocalDateTime.of(date, java.time.LocalTime.of(7, 0)),                  // wakeTime
                    lowWaterDays.contains(i) ? randInt(random, 500, 900) : randInt(random, 1500, 2500), // waterIntakeMl
                    random.nextDouble() < 0.1,  // isFasting (10% 확률)
                    random.nextDouble() < 0.2,  // isRestDay (20% 확률)
                    round1(1900f + randFloat(random, -200, 200)),                  // totalCaloriesKcal
                    round1(60f + randFloat(random, -10, 10)),                      // fatG
                    round1(45f + randFloat(random, -10, 10)),                      // sugarG
                    highSodiumDays.contains(i)
                            ? round1(3200f + randFloat(random, 0, 500))            // sodiumMg (상위 20% 유발)
                            : round1(1800f + randFloat(random, -300, 300)),
                    round1(20f + randFloat(random, -5, 5)),                        // deepSleepRatio
                    resolveMenstrualPhase(i),                                      // menstrualPhase (28일 주기 시뮬레이션)
                    clamp(5 + randInt(random, -2, 4), 0, 11),                      // uvIndex
                    round1(30f + randFloat(random, -10, 15))                       // pm25Value
            );

            dailyWellnessRecordRepository.save(record);
            savedCount++;
        }

        return savedCount;
    }

    private boolean isWithinStreak(Set<Integer> streakStartDays, int dayIndex, int streakLength) {
        for (int start : streakStartDays) {
            if (dayIndex >= start && dayIndex < start + streakLength) return true;
        }
        return false;
    }

    /** streakStartDays 각각의 (start + streakLength - 1)일, 즉 스트릭이 "끝난 날" 기준으로 lagMin~lagMax일 뒤인지 확인 */
    private boolean isTriggeredWithLag(Set<Integer> streakStartDays, int dayIndex, int streakLength, int lagMin, int lagMax) {
        for (int start : streakStartDays) {
            int streakEnd = start + streakLength - 1;
            int diff = dayIndex - streakEnd;
            if (diff >= lagMin && diff <= lagMax) return true;
        }
        return false;
    }

    private MenstrualPhase resolveMenstrualPhase(int dayIndex) {
        int cycleDay = dayIndex % 28;
        if (cycleDay < 5) return MenstrualPhase.MENSTRUAL;
        if (cycleDay < 13) return MenstrualPhase.FOLLICULAR;
        if (cycleDay < 15) return MenstrualPhase.OVULATION;
        return MenstrualPhase.LUTEAL;
    }

    private int randInt(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private float randFloat(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float round1(float value) {
        return Math.round(value * 10) / 10f;
    }
}
