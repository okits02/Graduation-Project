package com.example.rating_service.repository;

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
            ROUND(AVG(r.rating_score), 1),
            COUNT(*)
        FROM rating r
        WHERE r.product_id = :productId
    """, nativeQuery = true)
    Object[] getRatingSummary(@Param("productId") String productId);
}
