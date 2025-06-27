package com.example.promotion_service.repository;

import com.example.promotion_service.model.Promotion;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    boolean existsByName(@NotNull String name);
    Promotion findByName(@NotNull String name);
    @Query("Select p form Promotion p where p.isActive = true and p.endDate<:now")
    List<Promotion> findExpiredPromotion(@Param("now")LocalDateTime now);
    void delete(String promotionId);
}
