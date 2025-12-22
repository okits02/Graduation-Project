package com.example.product_service.mapper;

import com.example.product_service.dto.request.BrandCreationRequest;
import com.example.product_service.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface BrandMapper {
    void updateBrand(@MappingTarget Brand brand, BrandCreationRequest request);
}
