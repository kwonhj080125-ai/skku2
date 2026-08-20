package com.example.skku2_backend.dailyrecord.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_wellness_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_daily_record",
                columnNames = {"user_id", "logged_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyWellnessRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;

    // ===== 1. 피부 진단 및 컨디션 데이터 =====
    @Column(name = "daily_skin_score")
    private Integer dailySkinScore;

    @Column(name = "skin_age")
    private Integer skinAge;

    @Column(name = "oily_intensity_score")
    private Integer oilyIntensityScore;

    @Column(name = "blackhead_score")
    private Integer blackheadScore;

    @Column(name = "pores_score")
    private Integer poresScore;

    // ===== 2. 체중 및 정밀 검사 데이터 =====
    @Column(name = "morning_weight_kg")
    private Float morningWeightKg;

    @Column(name = "evening_weight_kg")
    private Float eveningWeightKg;

    @Column(name = "body_water_mass_kg")
    private Float bodyWaterMassKg;

    @Column(name = "skeletal_muscle_mass_kg")
    private Float skeletalMuscleMassKg;

    @Column(name = "body_fat_mass_kg")
    private Float bodyFatMassKg;

    @Column(name = "subcutaneous_fat_mm")
    private Float subcutaneousFatMm;

    @Column(name = "body_fat_percentage")
    private Float bodyFatPercentage;

    // ===== 3. 수면 / 수분 / 식단 / 운동 데이터 =====
    @Column(name = "sleep_duration_minutes")
    private Integer sleepDurationMinutes;

    @Column(name = "bedtime")
    private LocalDateTime bedtime;

    @Column(name = "wake_time")
    private LocalDateTime wakeTime;

    @Column(name = "water_intake_ml")
    private Integer waterIntakeMl;

    @Column(name = "is_fasting", nullable = false)
    private Boolean isFasting = false;

    @Column(name = "is_rest_day", nullable = false)
    private Boolean isRestDay = false;

    // ===== 4. 기타 웰니스 서비스 연동 데이터 =====
    @Column(name = "total_calories_kcal")
    private Float totalCaloriesKcal;

    @Column(name = "fat_g")
    private Float fatG;

    @Column(name = "sugar_g")
    private Float sugarG;

    @Column(name = "sodium_mg")
    private Float sodiumMg;

    @Column(name = "deep_sleep_ratio")
    private Float deepSleepRatio;

    @Enumerated(EnumType.STRING)
    @Column(name = "menstrual_phase")
    private MenstrualPhase menstrualPhase;

    @Column(name = "uv_index")
    private Integer uvIndex;

    @Column(name = "pm25_value")
    private Float pm25Value;

    // ===== 5. 메타데이터 =====
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static DailyWellnessRecord createOrUpdate(
            DailyWellnessRecord existing,
            Long userId,
            LocalDate loggedDate,
            Integer dailySkinScore,
            Integer skinAge,
            Integer oilyIntensityScore,
            Integer blackheadScore,
            Integer poresScore,
            Float morningWeightKg,
            Float eveningWeightKg,
            Float bodyWaterMassKg,
            Float skeletalMuscleMassKg,
            Float bodyFatMassKg,
            Float subcutaneousFatMm,
            Float bodyFatPercentage,
            Integer sleepDurationMinutes,
            LocalDateTime bedtime,
            LocalDateTime wakeTime,
            Integer waterIntakeMl,
            Boolean isFasting,
            Boolean isRestDay,
            Float totalCaloriesKcal,
            Float fatG,
            Float sugarG,
            Float sodiumMg,
            Float deepSleepRatio,
            MenstrualPhase menstrualPhase,
            Integer uvIndex,
            Float pm25Value
    ) {
        DailyWellnessRecord record = existing != null ? existing : new DailyWellnessRecord();
        record.userId = userId;
        record.loggedDate = loggedDate;
        record.dailySkinScore = dailySkinScore;
        record.skinAge = skinAge;
        record.oilyIntensityScore = oilyIntensityScore;
        record.blackheadScore = blackheadScore;
        record.poresScore = poresScore;
        record.morningWeightKg = morningWeightKg;
        record.eveningWeightKg = eveningWeightKg;
        record.bodyWaterMassKg = bodyWaterMassKg;
        record.skeletalMuscleMassKg = skeletalMuscleMassKg;
        record.bodyFatMassKg = bodyFatMassKg;
        record.subcutaneousFatMm = subcutaneousFatMm;
        record.bodyFatPercentage = bodyFatPercentage;
        record.sleepDurationMinutes = sleepDurationMinutes;
        record.bedtime = bedtime;
        record.wakeTime = wakeTime;
        record.waterIntakeMl = waterIntakeMl;
        record.isFasting = isFasting != null ? isFasting : false;
        record.isRestDay = isRestDay != null ? isRestDay : false;
        record.totalCaloriesKcal = totalCaloriesKcal;
        record.fatG = fatG;
        record.sugarG = sugarG;
        record.sodiumMg = sodiumMg;
        record.deepSleepRatio = deepSleepRatio;
        record.menstrualPhase = menstrualPhase;
        record.uvIndex = uvIndex;
        record.pm25Value = pm25Value;
        return record;
    }
}
