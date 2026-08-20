package com.example.skku2_backend.wellness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WellnessCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    @NotNull(message = "참여자당 1주 비용은 필수입니다.")
    @Positive(message = "비용은 0보다 커야 합니다.")
    private Long weeklyCostPerParticipant;

    @NotEmpty(message = "수집 데이터 종류는 최소 1개 이상이어야 합니다.")
    private List<String> collectedDataTypes;
}
