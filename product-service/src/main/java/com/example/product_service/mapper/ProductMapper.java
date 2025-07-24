package com.example.product_service.mapper;

import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.model.Products;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Products toProduct(ProductRequest request);
    ProductResponse toProductResponse(Products products);
    void updateProduct(@MappingTarget Products products, ProductUpdateRequest productRequest);
}
