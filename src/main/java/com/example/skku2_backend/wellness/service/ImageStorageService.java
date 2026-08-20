package com.example.skku2_backend.wellness.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file, String prefix) {
        validateImage(file);
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String ext = extractExtension(file.getOriginalFilename());
            String fileName = prefix + "_" + UUID.randomUUID() + ext;
            Path target = dir.resolve(fileName);
            file.transferTo(target);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            log.error("이미지 저장 실패", e);
            throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
        }
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
