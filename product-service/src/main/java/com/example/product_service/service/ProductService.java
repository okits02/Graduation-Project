package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductSearchRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.model.Products;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    public Page<ProductResponse> getAll(int page, int size);
    public Page<ProductResponse> searchProducts(ProductSearchRequest request);
    public ProductResponse getById(String productId);
    public Products createProduct(ProductRequest request);
    public ProductResponse updateProduct(ProductRequest request);
    public void DeleteProduct(String productId);
}
