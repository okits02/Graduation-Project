package com.example.promotion_service.repository;

import com.example.promotion_service.enums.PromotionKind;
import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.model.PromotionApplyTo;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    boolean existsByName(@NotNull String name);
    Promotion findByName(@NotNull String name);
    @Query("Select p from Promotion p where p.active = true and p.endDate<:now")
    List<Promotion> findExpiredPromotion(@Param("now")LocalDateTime now);
    void deleteById(String promotionId);
    boolean existsByVoucherCode(String voucherCode);
    Page<Promotion> findAllByPromotionKind(PromotionKind promotionKind, Pageable pageable);
    @Query("""
            SELECT p 
            FROM Promotion p
            WHERE FUNCTION('DATE', p.endDate) = :today
            AND p.active = true
            AND p.promotionKind = AUTO
            """)
    List<Promotion> findExpireToday(LocalDate today);
    @Query(value = """
    SELECT p.*
    FROM promotion p
    LEFT JOIN promotion_apply_to pa
        ON pa.promotion_id = p.id
    LEFT JOIN promotion_usage pu
        ON pu.promotion_id = p.id
       AND pu.user_id = :userId
    WHERE p.active = true
      AND p.start_date <= :today
      AND p.end_date >= :today
      AND p.minimum_order_purchase_amount <= :totalAmount
      AND p.usage_count < p.usage_limited
      AND (
            p.apply_to = 'ALL'
            OR (p.apply_to = 'PRODUCT' AND pa.product_id IN (:productIds))
            OR (p.apply_to = 'CATEGORY' AND pa.category_id IN (:categoryIds))
          )
    GROUP BY p.id
    HAVING (
        p.usage_limit_per_user IS NULL
        OR p.usage_limit_per_user = 0
        OR COUNT(pu.id) < p.usage_limit_per_user
    )
    """, nativeQuery = true)
    List<Promotion> findApplicablePromotions(
            @Param("today") LocalDate today,
            @Param("totalAmount") Double totalAmount,
            @Param("productIds") List<String> productIds,
            @Param("categoryIds") List<String> categoryIds,
            @Param("userId") String userId
    );

    @Query(value = """
            SELECT p.*
            FROM promotion p
            WHERE p.voucher_code = :voucherCode
            """, nativeQuery = true)
    Promotion findByVoucherCode(@Param("voucherCode") String voucherCode);

    @Query("""
    SELECT p
    FROM Promotion p
    LEFT JOIN FETCH p.promotionApplyTo
    WHERE p.active = false
      AND p.startDate <= :now
      AND p.endDate >= :now
    """)
    List<Promotion> findPromotionToActivate(LocalDateTime now);
}
