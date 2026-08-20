package com.example.skku2_backend.wellness.repository;

import com.example.skku2_backend.wellness.domain.Wellness;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WellnessRepository extends JpaRepository<Wellness, Long> {
}
