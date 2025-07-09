package com.example.promotion_service.mapper;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.example.promotion_service.model.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    Promotion toPromotion(PromotionCreationRequest request);
    PromotionResponse toPromotionResponse(Promotion promotion);
    void updatePromotion(@MappingTarget Promotion promotion, PromotionUpdateRequest request);
}
