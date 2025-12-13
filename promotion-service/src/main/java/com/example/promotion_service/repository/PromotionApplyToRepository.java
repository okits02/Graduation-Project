package com.example.promotion_service.repository;


import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.model.PromotionApplyTo;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionApplyToRepository extends JpaRepository<PromotionApplyTo, String> {
    @Query("Select p From PromotionApplyTo p Where p.promotion.id = :promotionId")
    PromotionApplyTo findByPromotionId(@NotNull String promotionId);
    @Query(value = "SELECT * FROM promotion_apply_to WHERE product_id = :productId AND promotion_id = :promotionId",
            nativeQuery = true)
    PromotionApplyTo findByProductIdAndPromotion(@Param("productId") String productId,
                                                 @Param("promotionId") String promotionId);
    @Query(value = "SELECT * FROM promotion_apply_to WHERE category_name = :categoryName AND promotion_id = :promotionId",
            nativeQuery = true)
    PromotionApplyTo findByCategoryNameAndPromotion(@Param("categoryName") String categoryName,
                                                    @Param("promotionId") String promotionId);

    boolean existsByPromotionAndProductId(Promotion promotion, String id);

    boolean existsByPromotionAndCategoryId(Promotion promotion, String id);
}
