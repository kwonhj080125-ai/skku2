package com.example.skku2_backend.cosmetic.controller;

import com.example.skku2_backend.cosmetic.dto.CosmeticCreateRequest;
import com.example.skku2_backend.cosmetic.dto.CosmeticResponse;
import com.example.skku2_backend.cosmetic.service.CosmeticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cosmetics")
@RequiredArgsConstructor
public class CosmeticController {

    private final CosmeticService cosmeticService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(
            @RequestPart("request") @Valid CosmeticCreateRequest request,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage
    ) {
        Long id = cosmeticService.create(request, productImage);
        return ResponseEntity.created(URI.create("/api/cosmetics/" + id)).build();
    }

    @GetMapping
    public ResponseEntity<List<CosmeticResponse>> getAll() {
        return ResponseEntity.ok(cosmeticService.getAll());
    }
}
