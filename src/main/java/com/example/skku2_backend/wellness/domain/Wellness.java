package com.example.skku2_backend.wellness.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 웰니스 서비스(제휴사) 엔티티.
 * 예: WHS, 필라이즈, PEP, DERNA 같은 타사 웰니스 서비스를 등록하는 단위.
 */
@Entity
@Table(name = "wellness")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wellness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SQLite의 AUTOINCREMENT와 매핑됨
    @Column(name = "wellness_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 이름

    @Lob
    @Column(nullable = false)
    private String description; // 설명

    @Column(name = "weekly_cost_per_participant", nullable = false)
    private Long weeklyCostPerParticipant; // 참여자당 1주 비용 (원 단위)

    // 로고는 DB에 이미지 자체(BLOB)를 넣지 않고, 파일로 저장한 뒤 경로/URL만 저장한다.
    // 이유: SQLite 파일 용량이 커지면 백업/이관이 불편하고, 조회 성능도 떨어짐.
    @Column(name = "logo_image_url")
    private String logoImageUrl; // 로고

    // 수집 데이터 종류 (예: "수면", "식단", "체중", "생리주기" 등 복수 값)
    // 컬럼 하나에 콤마로 이어붙이지 않고 별도 테이블로 정규화한다.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "wellness_collected_data_type",
            joinColumns = @JoinColumn(name = "wellness_id")
    )
    @Column(name = "data_type", nullable = false)
    private List<String> collectedDataTypes = new ArrayList<>();

    @Builder
    public Wellness(String name, String description, Long weeklyCostPerParticipant,
                     String logoImageUrl, List<String> collectedDataTypes) {
        this.name = name;
        this.description = description;
        this.weeklyCostPerParticipant = weeklyCostPerParticipant;
        this.logoImageUrl = logoImageUrl;
        this.collectedDataTypes = collectedDataTypes != null ? collectedDataTypes : new ArrayList<>();
    }

    public void update(String name, String description, Long weeklyCostPerParticipant,
                        List<String> collectedDataTypes) {
        this.name = name;
        this.description = description;
        this.weeklyCostPerParticipant = weeklyCostPerParticipant;
        this.collectedDataTypes = collectedDataTypes;
    }

    public void updateLogoImageUrl(String logoImageUrl) {
        this.logoImageUrl = logoImageUrl;
    }
}
