package com.example.product_service.service;

import com.example.product_service.dto.request.ProductVariantsRequest;
import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.model.ProductVariants;

import java.util.List;

public interface ProductVariantsService {
    public List<String> save(List<ProductVariantsRequest> request, String productId);
    public List<String> update(List<ProductVariantsRequest> request, String productId);
    public List<ProductVariantsResponse> getListByProductId(String productId);
    public List<ProductVariants> getVariantForKafkaEvent(String productId);
    public void deleteBySku(String sku);
    public void deleteByProductId(String productId);
    public void changeStock(String sku, Boolean inStock);
    public ProductVariantsResponse getVariantBySku(String sku);
}
