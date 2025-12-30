package com.example.promotion_service.repository;

import com.example.promotion_service.model.PromotionUsage;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, String> {
    @Query(value = """
            SELECT * 
            FROM promotion_usage
            WHERE order_id = :orderId
            LIMIT 1
            """, nativeQuery = true)
    PromotionUsage findByOrderId(String orderId);

    @Query(value = """
            SELECT COUNT(*) 
            FROM promotion_usage
            WHERE user_id = :userId
            AND promotion_id = :promotionId
            """, nativeQuery = true)
    long countByPromotionIdAndUserId(String id, String userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM promotion_usage
            WHERE order_id = :orderId
            AND promotion_id = :promotionId
            """)
    long countByPromotionIdAndOrderId(String id, String orderId);
}
