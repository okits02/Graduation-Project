package com.example.promotion_service.mapper;

import com.example.promotion_service.dto.request.PromotionCampaignRequest;
import com.example.promotion_service.dto.response.PromotionCampaignResponse;
import com.example.promotion_service.model.PromotionCampaign;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromotionCampaignMapper {
    PromotionCampaign toPromotionCampaign(PromotionCampaignRequest request);
    PromotionCampaignResponse toResponse(PromotionCampaign promotionCampaign);
    void update(@MappingTarget PromotionCampaign promotionCampaign, PromotionCampaignRequest request);
}
