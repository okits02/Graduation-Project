package com.example.product_service.mapper;

import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.model.Products;
import org.mapstruct.Mapper;

@Mapper(componentModel = "sping")
public interface ProductMapper {
    Products toProduct(ProductRequest request);
    ProductResponse toProductResponse(Products products);
}
