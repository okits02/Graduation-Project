package com.example.product_service.mapper;

import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.request.ProductVariantsRequest;
import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.model.ProductVariants;
import com.example.product_service.model.Products;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductVariantsMapper {
    ProductVariants toProductVariants(ProductVariantsRequest request);
    ProductVariantsResponse toProductVariantsResponse(ProductVariants productVariants);
    void updateProduct(@MappingTarget ProductVariants productVariants,
                       ProductVariantsRequest productRequest);
}
