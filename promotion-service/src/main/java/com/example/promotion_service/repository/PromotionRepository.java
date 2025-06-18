package com.example.promotion_service.repository;

import com.example.promotion_service.model.Promotion;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    boolean existsByName(@NotNull String name);
    Promotion findByName(@NotNull String name);

    void delete(String promotionId);
}
