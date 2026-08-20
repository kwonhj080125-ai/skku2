package com.example.skku2_backend.wellness.dto;

import com.example.skku2_backend.wellness.domain.Wellness;
import lombok.Getter;

import java.util.List;

@Getter
public class WellnessResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final Long weeklyCostPerParticipant;
    private final String logoImageUrl;
    private final List<String> collectedDataTypes;

    public WellnessResponse(Wellness wellness) {
        this.id = wellness.getId();
        this.name = wellness.getName();
        this.description = wellness.getDescription();
        this.weeklyCostPerParticipant = wellness.getWeeklyCostPerParticipant();
        this.logoImageUrl = wellness.getLogoImageUrl();
        this.collectedDataTypes = wellness.getCollectedDataTypes();
    }
}
