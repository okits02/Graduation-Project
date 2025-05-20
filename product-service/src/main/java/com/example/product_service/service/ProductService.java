package com.example.product_service.service;

import com.example.product_service.dto.PageResponse;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.model.Products;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    public PageResponse<ProductResponse> getAll(int page, int size);
    public ProductResponse getById(String productId);
    public Products createProduct(MultipartFile thumbNail, List<MultipartFile> multipartFile, ProductRequest request);
    public ProductResponse updateProduct(MultipartFile thumbNails, List<MultipartFile> multipartFiles, ProductUpdateRequest request);
    public void DeleteProduct(String productId);
}
