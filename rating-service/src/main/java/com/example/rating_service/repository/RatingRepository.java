package com.example.rating_service.repository;

import com.example.rating_service.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

public interface RatingRepository extends JpaRepository<Rating, String> {

    boolean existsByCreatedByAndProductId(String createBy, String productId);
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
}
