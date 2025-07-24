package com.example.search_service.mapper;

import com.example.search_service.model.Promotion;
import com.example.search_service.viewmodel.dto.ApplyPromotionEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    void updatePromotion(@MappingTarget Promotion promotion, ApplyPromotionEventDTO request);
}
