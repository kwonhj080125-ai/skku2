package com.example.skku2_backend.wellness.controller;

import com.example.skku2_backend.wellness.dto.WellnessCreateRequest;
import com.example.skku2_backend.wellness.dto.WellnessResponse;
import com.example.skku2_backend.wellness.service.WellnessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/wellness")
@RequiredArgsConstructor
public class WellnessController {

    private final WellnessService wellnessService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(
            @RequestPart("request") @Valid WellnessCreateRequest request,
            @RequestPart(value = "logoImage", required = false) MultipartFile logoImage
    ) {
        Long id = wellnessService.create(request, logoImage);
        return ResponseEntity.created(URI.create("/api/wellness/" + id)).build();
    }

    @GetMapping("/{wellnessId}")
    public ResponseEntity<WellnessResponse> getOne(@PathVariable Long wellnessId) {
        return ResponseEntity.ok(wellnessService.getOne(wellnessId));
    }

    @GetMapping
    public ResponseEntity<List<WellnessResponse>> getAll() {
        return ResponseEntity.ok(wellnessService.getAll());
    }

    @PutMapping("/{wellnessId}")
    public ResponseEntity<Void> update(
            @PathVariable Long wellnessId,
            @RequestBody @Valid WellnessCreateRequest request
    ) {
        wellnessService.update(wellnessId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{wellnessId}/logo", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateLogo(
            @PathVariable Long wellnessId,
            @RequestPart("logoImage") MultipartFile logoImage
    ) {
        wellnessService.updateLogo(wellnessId, logoImage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{wellnessId}")
    public ResponseEntity<Void> delete(@PathVariable Long wellnessId) {
        wellnessService.delete(wellnessId);
        return ResponseEntity.noContent().build();
    }
}
