package com.example.skku2_backend.cosmetic.service;

import com.example.skku2_backend.cosmetic.domain.Cosmetic;
import com.example.skku2_backend.cosmetic.dto.CosmeticCreateRequest;
import com.example.skku2_backend.cosmetic.dto.CosmeticResponse;
import com.example.skku2_backend.cosmetic.repository.CosmeticRepository;
import com.example.skku2_backend.wellness.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CosmeticService {

    private final CosmeticRepository cosmeticRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Long create(CosmeticCreateRequest request, MultipartFile productImage) {
        String productImageUrl = (productImage != null && !productImage.isEmpty())
                ? imageStorageService.store(productImage, "cosmetic")
                : null;

        Cosmetic cosmetic = Cosmetic.builder()
                .productImageUrl(productImageUrl)
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .selectedWellnessIds(request.getSelectedWellnessIds())
                .cost(request.getCost())
                .participantCount(request.getParticipantCount())
                .build();

        return cosmeticRepository.save(cosmetic).getId();
    }

    public List<CosmeticResponse> getAll() {
        return cosmeticRepository.findAll().stream()
                .map(CosmeticResponse::new)
                .collect(Collectors.toList());
    }
}
