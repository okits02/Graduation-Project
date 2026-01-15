package com.example.promotion_service.services;

import com.example.promotion_service.dto.request.PromotionCampaignRequest;
import com.example.promotion_service.dto.response.PromotionCampaignResponse;

import java.util.List;

public interface PromotionCampaignService {
    public PromotionCampaignResponse save(PromotionCampaignRequest request);
    public PromotionCampaignResponse update(PromotionCampaignRequest request);
    public void delete(String id);
    public void deleteAll();
    public List<PromotionCampaignResponse> getAll();
}
