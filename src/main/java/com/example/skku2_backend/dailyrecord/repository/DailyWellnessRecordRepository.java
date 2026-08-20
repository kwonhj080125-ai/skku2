package com.example.skku2_backend.dailyrecord.repository;

import com.example.skku2_backend.dailyrecord.domain.DailyWellnessRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyWellnessRecordRepository extends JpaRepository<DailyWellnessRecord, Long> {

    // 하루 1건 유니크 제약(uq_user_daily_record)과 짝이 되는 조회 메서드.
    // 같은 유저가 같은 날짜로 또 저장을 요청하면, 새로 만들지 않고 이 기록을 찾아서 덮어쓴다.
    Optional<DailyWellnessRecord> findByUserIdAndLoggedDate(Long userId, LocalDate loggedDate);

    // 상관관계 분석용: 특정 유저의 특정 기간(예: 최근 90일) 기록을 날짜순 List로 조회.
    // 이게 곧 "일별로 쌓인 데이터"를 다루는 방법 - 매번 새 List/Map을 직접 관리할 필요 없이
    // DB에 쌓인 행들을 그때그때 조회만 하면 됨.
    List<DailyWellnessRecord> findByUserIdAndLoggedDateBetweenOrderByLoggedDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate
    );
}
