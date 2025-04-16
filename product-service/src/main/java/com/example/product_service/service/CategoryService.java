package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
public interface CategoryService {
    public Page<CategoryResponse> finAll(int Page, int Size);
    public CategoryResponse findById(String categoryId);
    public CategoryResponse createCate(CategoryRequest request);
    public CategoryResponse updateCate(CategoryRequest request);
    public void deleteCateById(String categoryId);
}
