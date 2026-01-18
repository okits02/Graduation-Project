package com.example.search_service.mapper;

import com.example.search_service.model.ProductVariants;
import com.example.search_service.viewmodel.dto.response.VariantResponse;
import org.mapstruct.Mapper;
import org.springframework.data.elasticsearch.annotations.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    VariantResponse toResponse(ProductVariants productVariants);
}
