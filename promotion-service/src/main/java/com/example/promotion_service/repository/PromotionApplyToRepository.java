package com.example.promotion_service.repository;


import com.example.promotion_service.model.PromotionApplyTo;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PromotionApplyToRepository extends JpaRepository<PromotionApplyTo, String> {
    @Query("Select p From PromotionApplyTo p Where p.promotion.id = :promotionId")
    PromotionApplyTo findByPromotionId(@NotNull String promotionId);
}
