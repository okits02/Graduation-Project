package com.example.promotion_service.repository;


import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.model.PromotionApplyTo;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionApplyToRepository extends JpaRepository<PromotionApplyTo, String> {
    @Query("Select p From PromotionApplyTo p Where p.promotion.id = :promotionId")
    PromotionApplyTo findByPromotionId(@NotNull String promotionId);
    @Query(value = "SELECT * FROM promotion_apply_to WHERE product_id = :productId AND promotion_id = :promotionId",
            nativeQuery = true)
    PromotionApplyTo findByProductIdAndPromotion(@Param("productId") String productId,
                                                 @Param("promotionId") String promotionId);
    @Query(value = "SELECT * FROM promotion_apply_to WHERE category_id = :categoryId AND promotion_id = :promotionId",
            nativeQuery = true)
    PromotionApplyTo findByCategoryNameAndPromotion(@Param("categoryId") String categoryId,
                                                    @Param("promotionId") String promotionId);

    @Query(
            value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM promotion_apply_to
    WHERE promotion_id = :promotionId
      AND product_id = :productId
  """,
            nativeQuery = true
    )
    Long existsByPromotionAndProductId(
            @Param("promotionId") String promotionId,
            @Param("productId") String productId
    );
    @Query(
            value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM promotion_apply_to
    WHERE promotion_id = :promotionId
      AND category_id = :categoryId
  """,
            nativeQuery = true
    )
    Long existsByPromotionAndCategoryId(
            @Param("promotionId") String promotionId,
            @Param("categoryId") String categoryId
    );

    @Query(
            value = """
        SELECT *
        FROM promotion_apply_to
        WHERE category_id = :categoryId
    """,
            nativeQuery = true
    )
    List<PromotionApplyTo> findByCategoryId(@Param("categoryId") String categoryId);
}
