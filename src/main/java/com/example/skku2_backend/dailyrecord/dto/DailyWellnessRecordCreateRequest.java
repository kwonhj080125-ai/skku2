package com.example.skku2_backend.dailyrecord.dto;

import com.example.skku2_backend.dailyrecord.domain.MenstrualPhase;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 하루치 웰니스 기록 저장 요청.
 * userId, loggedDate만 필수이고 나머지는 그날 수집된 값만 채워서 보내면 됨
 * (모든 웰니스 서비스가 매일 모든 데이터를 다 주는 게 아니므로 나머지는 nullable).
 */
@Getter
@NoArgsConstructor
public class DailyWellnessRecordCreateRequest {

    @NotNull(message = "userId는 필수입니다.")
    private Long userId;

    @NotNull(message = "loggedDate는 필수입니다.")
    private LocalDate loggedDate;

    // 피부
    private Integer dailySkinScore;
    private Integer skinAge;
    private Integer oilyIntensityScore;
    private Integer blackheadScore;
    private Integer poresScore;

    // 체중/인바디
    private Float morningWeightKg;
    private Float eveningWeightKg;
    private Float bodyWaterMassKg;
    private Float skeletalMuscleMassKg;
    private Float bodyFatMassKg;
    private Float subcutaneousFatMm;
    private Float bodyFatPercentage;

    // 수면/수분/식사/운동
    private Integer sleepDurationMinutes;
    private LocalDateTime bedtime;
    private LocalDateTime wakeTime;
    private Integer waterIntakeMl;
    private Boolean isFasting;
    private Boolean isRestDay;

    // 영양소
    private Float totalCaloriesKcal;
    private Float fatG;
    private Float sugarG;
    private Float sodiumMg;

    // 수면 정밀
    private Float deepSleepRatio;

    // 여성 건강
    private MenstrualPhase menstrualPhase;

    // 환경
    private Integer uvIndex;
    private Float pm25Value;
}
