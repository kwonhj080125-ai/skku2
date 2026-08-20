package com.example.skku2_backend.cosmetic.dto;

import com.example.skku2_backend.cosmetic.domain.Cosmetic;
import lombok.Getter;

import java.util.List;

@Getter
public class CosmeticResponse {

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
