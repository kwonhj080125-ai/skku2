package com.example.skku2_backend.wellness.service;

import com.example.skku2_backend.wellness.domain.Wellness;
import com.example.skku2_backend.wellness.dto.WellnessCreateRequest;
import com.example.skku2_backend.wellness.dto.WellnessResponse;
import com.example.skku2_backend.wellness.repository.WellnessRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WellnessService {

    private final WellnessRepository wellnessRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Long create(WellnessCreateRequest request, MultipartFile logoImage) {
        String logoImageUrl = (logoImage != null && !logoImage.isEmpty())
                ? imageStorageService.store(logoImage, "wellness-logo")
                : null;

        Wellness wellness = Wellness.builder()
                .name(request.getName())
                .description(request.getDescription())
                .weeklyCostPerParticipant(request.getWeeklyCostPerParticipant())
                .logoImageUrl(logoImageUrl)
                .collectedDataTypes(request.getCollectedDataTypes())
                .build();

        return wellnessRepository.save(wellness).getId();
    }

    public WellnessResponse getOne(Long wellnessId) {
        return new WellnessResponse(findById(wellnessId));
    }

    public List<WellnessResponse> getAll() {
        return wellnessRepository.findAll().stream()
                .map(WellnessResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long wellnessId, WellnessCreateRequest request) {
        Wellness wellness = findById(wellnessId);
        wellness.update(
                request.getName(),
                request.getDescription(),
                request.getWeeklyCostPerParticipant(),
                request.getCollectedDataTypes()
        );
    }

    @Transactional
    public void updateLogo(Long wellnessId, MultipartFile logoImage) {
        Wellness wellness = findById(wellnessId);
        String logoImageUrl = imageStorageService.store(logoImage, "wellness-logo");
        wellness.updateLogoImageUrl(logoImageUrl);
    }

    @Transactional
    public void delete(Long wellnessId) {
        wellnessRepository.delete(findById(wellnessId));
    }

    private Wellness findById(Long wellnessId) {
        return wellnessRepository.findById(wellnessId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 웰니스입니다. id=" + wellnessId));
    }
}
