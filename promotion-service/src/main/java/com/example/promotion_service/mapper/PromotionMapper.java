package com.example.promotion_service.mapper;

import com.example.promotion_service.dto.request.FlashSaleCreationRequest;
import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.model.PromotionApplyTo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    Promotion toPromotion(PromotionCreationRequest request);
    Promotion toPromotionFlashSale(FlashSaleCreationRequest request);
    @Mapping(target = "productId", expression = "java(mapProductIds(promotion))")
    @Mapping(target = "categoryId", expression = "java(mapCategoryIds(promotion))")
    PromotionResponse toPromotionResponse(Promotion promotion);
    void updatePromotion(@MappingTarget Promotion promotion, PromotionUpdateRequest request);

    default List<String> mapProductIds(Promotion promotion){
        if(promotion.getPromotionApplyTo() == null || promotion.getPromotionApplyTo().isEmpty()){
            return new ArrayList<>();
        }
        return promotion.getPromotionApplyTo().stream()
                .map(PromotionApplyTo::getProductId)
                .filter(Objects::nonNull)
                .toList();
    }

    default List<String> mapCategoryIds(Promotion promotion){
        if(promotion.getPromotionApplyTo() == null || promotion.getPromotionApplyTo().isEmpty()){
            return new ArrayList<>();
        }
        return promotion.getPromotionApplyTo().stream()
                .map(PromotionApplyTo::getCategoryId)
                .filter(Objects::nonNull)
                .toList();
    }
}
