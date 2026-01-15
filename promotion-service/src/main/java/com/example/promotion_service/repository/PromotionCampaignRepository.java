package com.example.promotion_service.repository;

import com.example.promotion_service.model.PromotionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionCampaignRepository extends JpaRepository<PromotionCampaign, String> {

    boolean existsByName(String name);
}
