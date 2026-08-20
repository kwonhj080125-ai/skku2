package com.example.skku2_backend.cosmetic.dto;

import com.example.skku2_backend.cosmetic.domain.Cosmetic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CosmeticCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "브랜드는 필수입니다.")
    private String brand;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    @NotEmpty(message = "연동할 웰니스를 최소 1개 이상 선택해야 합니다.")
    private List<String> selectedWellnessIds;

    @NotNull(message = "비용은 필수입니다.")
    @Positive(message = "비용은 0보다 커야 합니다.")
    private Long cost;

    @NotNull(message = "인원은 필수입니다.")
    @Positive(message = "인원은 0보다 커야 합니다.")
    private Integer participantCount;

    @Getter
    public static class CosmeticResponse {

        private final Long id;
        private final String productImageUrl;
        private final String name;
        private final String brand;
        private final String description;
        private final List<String> selectedWellnessIds;
        private final Long cost;
        private final Integer participantCount;

        public CosmeticResponse(Cosmetic cosmetic) {
            this.id = cosmetic.getId();
            this.productImageUrl = cosmetic.getProductImageUrl();
            this.name = cosmetic.getName();
            this.brand = cosmetic.getBrand();
            this.description = cosmetic.getDescription();
            this.selectedWellnessIds = cosmetic.getSelectedWellnessIds();
            this.cost = cosmetic.getCost();
            this.participantCount = cosmetic.getParticipantCount();
        }
    }
}
