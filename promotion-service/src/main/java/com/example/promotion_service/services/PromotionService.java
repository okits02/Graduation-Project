package com.example.promotion_service.services;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.dto.response.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.example.promotion_service.model.Promotion;
import org.springframework.http.ResponseEntity;

public interface PromotionService {
    public PromotionResponse createPromotion(PromotionCreationRequest request);
    public Promotion updatePromotion(PromotionUpdateRequest request);
    public PromotionResponse getPromotion(String promotionId);
    public PageResponse<PromotionResponse> getAllPromotion(int page, int size);
    public void deletePromotion(String promotionId);
}
