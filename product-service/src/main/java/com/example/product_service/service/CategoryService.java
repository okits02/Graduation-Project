package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService {
    public Page<CategoryResponse> finAll(int Page, int Size);
    public CategoryResponse findById(String categoryId);
    public CategoryResponse createCate(MultipartFile multipartFile, CategoryRequest request);
    public CategoryResponse updateCate(CategoryRequest request);
    public void deleteCateById(String categoryId);
}
