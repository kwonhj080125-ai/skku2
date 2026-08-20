package com.example.skku2_backend.cosmetic.repository;

import com.example.skku2_backend.cosmetic.domain.Cosmetic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CosmeticRepository extends JpaRepository<Cosmetic, Long> {
}
