package com.example.skku2_backend.cosmetic.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cosmetic")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cosmetic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cosmetic_id")
    private Long id;

    @Column(name = "product_image_url")
    private String productImageUrl; // 이미지

    @Column(nullable = false, length = 100)
    private String name; // 이름

    @Column(nullable = false, length = 100)
    private String brand; // 브랜드

    @Lob
    @Column(nullable = false)
    private String description; // 설명

    // 선택한 연동 웰니스 (Wellness.id를 문자열로 저장)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "cosmetic_selected_wellness",
            joinColumns = @JoinColumn(name = "cosmetic_id")
    )
    @Column(name = "wellness_id", nullable = false)
    private List<String> selectedWellnessIds = new ArrayList<>();

    @Column(nullable = false)
    private Long cost; // 비용

    @Column(nullable = false)
    private Integer participantCount; // 인원

    @Builder
    public Cosmetic(String productImageUrl, String name, String brand, String description,
                     List<String> selectedWellnessIds, Long cost, Integer participantCount) {
        this.productImageUrl = productImageUrl;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.selectedWellnessIds = selectedWellnessIds != null ? selectedWellnessIds : new ArrayList<>();
        this.cost = cost;
        this.participantCount = participantCount;
    }
}
