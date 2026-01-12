package com.example.rating_service.repository;

import com.example.rating_service.dto.projector.RatingSummaryProjection;
import com.example.rating_service.model.Rating;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

public interface RatingRepository extends JpaRepository<Rating, String> {

    boolean existsByUserIdAndProductId(String userId, String productId);
    Page<Rating> findAllByProductId(
            String productId,
            Pageable pageable
    );

    Page<Rating> findAllByProductIdAndIsVerifiedPurchaseTrue(
            String productId,
            Pageable pageable
    );

    Page<Rating> findAllByProductIdAndRatingScore(
            String productId,
            Double ratingScore,
            Pageable pageable
    );

    @Query(value = """
    SELECT
        COALESCE(ROUND(AVG(r.rating_score)::numeric, 1), 0) AS average,
        COUNT(*) AS total
    FROM rating r
    WHERE r.product_id = :productId
    """, nativeQuery = true)
    RatingSummaryProjection getRatingSummary(@Param("productId") String productId);

    @Query("""
        SELECT AVG(r.ratingScore)
        FROM Rating r
        WHERE r.productId = :productId
    """)
    Double calculateAvgRatingByProductId(String productId);
}
