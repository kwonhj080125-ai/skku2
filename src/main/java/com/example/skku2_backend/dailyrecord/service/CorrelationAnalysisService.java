package com.example.skku2_backend.dailyrecord.service;

import com.example.skku2_backend.dailyrecord.domain.DailyWellnessRecord;
import com.example.skku2_backend.dailyrecord.dto.CorrelationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * "피부가 나빠졌을 때, 그 며칠 전에 뭐가 달랐는지"를 모든 변수에 대해 자동으로 찾아내는 엔진.
 *
 * 특정 변수 몇 개만 미리 정해서 규칙을 짜는 대신, 스키마에 있는 거의 모든 원인 후보 변수 x
 * 1~5일 시차 x 상/하위 20% 방향 조합을 전부 훑어서, 표본이 충분한(3건 이상) 조합만 반환한다.
 *
 * 개인화 포인트: 상/하위 20% 기준은 매번 "그 유저 자신의" 데이터 분포에서 계산하므로,
 * 사람마다 원래 나트륨을 많이 먹는지 적게 먹는지와 무관하게 "그 사람 기준 이례적으로 많았던 날"을 잡아낸다.
 */
@Service
public class CorrelationAnalysisService {

    private static final int MIN_LAG_DAYS = 1;
    private static final int MAX_LAG_DAYS = 5;
    private static final double PERCENTILE = 0.8; // 상/하위 20%

    // ── 원인 후보로 스캔할 수치형 변수들 (한글 라벨 → 값 추출 함수) ─────────────
    private static final Map<String, Function<DailyWellnessRecord, Float>> NUMERIC_FACTORS = new LinkedHashMap<>();
    static {
        NUMERIC_FACTORS.put("수면 시간", r -> toFloat(r.getSleepDurationMinutes()));
        NUMERIC_FACTORS.put("수분 섭취량", r -> toFloat(r.getWaterIntakeMl()));
        NUMERIC_FACTORS.put("총 섭취 칼로리", DailyWellnessRecord::getTotalCaloriesKcal);
        NUMERIC_FACTORS.put("지방 섭취량", DailyWellnessRecord::getFatG);
        NUMERIC_FACTORS.put("당류 섭취량", DailyWellnessRecord::getSugarG);
        NUMERIC_FACTORS.put("나트륨 섭취량", DailyWellnessRecord::getSodiumMg);
        NUMERIC_FACTORS.put("깊은 수면 비율", DailyWellnessRecord::getDeepSleepRatio);
        NUMERIC_FACTORS.put("자외선 지수", r -> toFloat(r.getUvIndex()));
        NUMERIC_FACTORS.put("초미세먼지 농도", DailyWellnessRecord::getPm25Value);
        NUMERIC_FACTORS.put("체지방률", DailyWellnessRecord::getBodyFatPercentage);
        NUMERIC_FACTORS.put("아침 체중", DailyWellnessRecord::getMorningWeightKg);
        NUMERIC_FACTORS.put("저녁 체중", DailyWellnessRecord::getEveningWeightKg);
        NUMERIC_FACTORS.put("체수분량", DailyWellnessRecord::getBodyWaterMassKg);
        NUMERIC_FACTORS.put("골격근량", DailyWellnessRecord::getSkeletalMuscleMassKg);
        NUMERIC_FACTORS.put("체지방량", DailyWellnessRecord::getBodyFatMassKg);
        NUMERIC_FACTORS.put("피하지방 두께", DailyWellnessRecord::getSubcutaneousFatMm);
    }

    // ── 원인 후보로 스캔할 boolean 변수들 ─────────────
    private static final Map<String, Predicate<DailyWellnessRecord>> BOOLEAN_FACTORS = Map.of(
            "단식 여부", r -> Boolean.TRUE.equals(r.getIsFasting()),
            "휴식일 여부", r -> Boolean.TRUE.equals(r.getIsRestDay())
    );

    public List<CorrelationResult> analyze(List<DailyWellnessRecord> records) {
        if (records.size() < 10) {
            return List.of(); // 표본이 너무 적으면 상/하위 20% 자체가 의미 없음
        }

        Map<LocalDate, DailyWellnessRecord> byDate = records.stream()
                .collect(Collectors.toMap(DailyWellnessRecord::getLoggedDate, r -> r, (a, b) -> a));

        double baselineTrouble = averageTroubleScore(records);

        // "피부 악화 발생일" 집합을 먼저 한 번에 계산 (모든 원인 후보가 공통으로 참조하는 응답 이벤트)
        Set<LocalDate> worsenedDates = records.stream()
                .map(DailyWellnessRecord::getLoggedDate)
                .filter(date -> isSkinWorsened(byDate, date, baselineTrouble))
                .collect(Collectors.toSet());

        List<CorrelationResult> results = new ArrayList<>();

        // 모든 수치형 변수 x 1~5일 시차 x 상/하위 20%를 전부 스캔
        for (Map.Entry<String, Function<DailyWellnessRecord, Float>> factor : NUMERIC_FACTORS.entrySet()) {
            for (int lag = MIN_LAG_DAYS; lag <= MAX_LAG_DAYS; lag++) {
                results.addAll(scanNumericFactor(records, byDate, worsenedDates, factor.getKey(), factor.getValue(), lag));
            }
        }

        // boolean 변수도 동일하게 스캔
        for (Map.Entry<String, Predicate<DailyWellnessRecord>> factor : BOOLEAN_FACTORS.entrySet()) {
            for (int lag = MIN_LAG_DAYS; lag <= MAX_LAG_DAYS; lag++) {
                CorrelationResult r = scanBooleanFactor(records, byDate, worsenedDates, factor.getKey(), factor.getValue(), lag);
                if (r != null) results.add(r);
            }
        }

        // 표본 3건 미만 필터링 + ratio 내림차순 정렬
        // (여기서 나온 결과가 많을 수 있으니, GPT 프롬프트에 넣기 전엔 상위 N개만 추려서 쓰는 걸 추천)
        return results.stream()
                .filter(r -> r.getTotal() >= 3)
                .sorted(Comparator.comparingDouble(CorrelationResult::getRatio).reversed())
                .collect(Collectors.toList());
    }

    /** 수치형 변수 하나에 대해, 상위 20%/하위 20% 두 방향 모두 검사해서 결과 리스트 반환 */
    private List<CorrelationResult> scanNumericFactor(List<DailyWellnessRecord> records,
                                                        Map<LocalDate, DailyWellnessRecord> byDate,
                                                        Set<LocalDate> worsenedDates,
                                                        String factorName,
                                                        Function<DailyWellnessRecord, Float> getter,
                                                        int lag) {
        List<CorrelationResult> out = new ArrayList<>();
        for (boolean top : List.of(true, false)) {
            List<LocalDate> eventDates = findPercentileEvents(records, getter, PERCENTILE, top);
            int[] occurrenceTotal = countResponses(byDate, worsenedDates, eventDates, lag);

            if (occurrenceTotal[1] >= 3) {
                String direction = top ? "상위 20%" : "하위 20%";
                out.add(new CorrelationResult(
                        factorName + " " + direction,
                        "피부 악화(점수 하락 또는 트러블 증가)",
                        lag + "일 후",
                        occurrenceTotal[0], occurrenceTotal[1]
                ));
            }
        }
        return out;
    }

    /** boolean 변수 하나에 대해 검사 */
    private CorrelationResult scanBooleanFactor(List<DailyWellnessRecord> records,
                                                 Map<LocalDate, DailyWellnessRecord> byDate,
                                                 Set<LocalDate> worsenedDates,
                                                 String factorName,
                                                 Predicate<DailyWellnessRecord> condition,
                                                 int lag) {
        List<LocalDate> eventDates = records.stream()
                .filter(condition)
                .map(DailyWellnessRecord::getLoggedDate)
                .collect(Collectors.toList());

        int[] occurrenceTotal = countResponses(byDate, worsenedDates, eventDates, lag);
        if (occurrenceTotal[1] < 3) return null;

        return new CorrelationResult(
                factorName,
                "피부 악화(점수 하락 또는 트러블 증가)",
                lag + "일 후",
                occurrenceTotal[0], occurrenceTotal[1]
        );
    }

    /** 이벤트 발생일 + lag일 뒤 시점에 "피부 악화"가 있었는지 카운트. 반환: {occurrence, total} */
    private int[] countResponses(Map<LocalDate, DailyWellnessRecord> byDate,
                                  Set<LocalDate> worsenedDates,
                                  List<LocalDate> eventDates,
                                  int lag) {
        int occurrence = 0;
        int total = 0;
        for (LocalDate eventDate : eventDates) {
            LocalDate checkDate = eventDate.plusDays(lag);
            if (!byDate.containsKey(checkDate)) continue; // 관찰 기간에 기록 자체가 없으면 표본 제외
            total++;
            if (worsenedDates.contains(checkDate)) occurrence++;
        }
        return new int[]{occurrence, total};
    }

    /**
     * "피부가 나빠졌다"의 공통 정의:
     * - 어제보다 daily_skin_score가 5점 이상 떨어졌거나
     * - 어제보다 트러블 점수(유분+블랙헤드+모공 평균)가 유의미하게 올랐으면 악화로 판정
     */
    private boolean isSkinWorsened(Map<LocalDate, DailyWellnessRecord> byDate, LocalDate date, double baselineTrouble) {
        DailyWellnessRecord today = byDate.get(date);
        DailyWellnessRecord yesterday = byDate.get(date.minusDays(1));
        if (today == null || yesterday == null) return false;

        boolean scoreDropped = false;
        if (today.getDailySkinScore() != null && yesterday.getDailySkinScore() != null) {
            scoreDropped = (yesterday.getDailySkinScore() - today.getDailySkinScore()) >= 5;
        }

        boolean troubleIncreased = false;
        Double todayTrouble = troubleScore(today);
        Double yesterdayTrouble = troubleScore(yesterday);
        if (todayTrouble != null && yesterdayTrouble != null) {
            double threshold = Math.max(3.0, baselineTrouble * 0.1); // 최소 3점 또는 평균의 10% 중 큰 값
            troubleIncreased = (todayTrouble - yesterdayTrouble) >= threshold;
        }

        return scoreDropped || troubleIncreased;
    }

    /** 상위/하위 20% 등 분포 기반 이벤트 날짜 리스트. 표본 10일 미만이면 의미 없으므로 빈 리스트 반환 */
    private List<LocalDate> findPercentileEvents(List<DailyWellnessRecord> records,
                                                  Function<DailyWellnessRecord, Float> getter,
                                                  double percentile, boolean top) {
        List<Float> values = records.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (values.size() < 10) {
            return List.of();
        }

        int idx = (int) Math.floor((top ? percentile : 1 - percentile) * (values.size() - 1));
        float threshold = values.get(idx);

        return records.stream()
                .filter(r -> {
                    Float v = getter.apply(r);
                    return v != null && (top ? v >= threshold : v <= threshold);
                })
                .map(DailyWellnessRecord::getLoggedDate)
                .collect(Collectors.toList());
    }

    private Double troubleScore(DailyWellnessRecord r) {
        if (r == null) return null;
        Integer oily = r.getOilyIntensityScore();
        Integer blackhead = r.getBlackheadScore();
        Integer pores = r.getPoresScore();
        if (oily == null || blackhead == null || pores == null) return null;
        return (oily + blackhead + pores) / 3.0;
    }

    private double averageTroubleScore(List<DailyWellnessRecord> records) {
        return records.stream()
                .map(this::troubleScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private static Float toFloat(Integer value) {
        return value == null ? null : value.floatValue();
    }
}
