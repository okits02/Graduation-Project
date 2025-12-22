package com.example.product_service.mapper;

import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.request.ProductVariantsRequest;
import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.model.Product_variants;
import com.example.product_service.model.Products;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductVariantsMapper {
    Product_variants toProductVariants(ProductVariantsRequest request);
    ProductVariantsResponse toProductVariantsResponse(Product_variants productVariants);
    void updateProduct(@MappingTarget Product_variants productVariants,
                       ProductVariantsRequest productRequest);
}
