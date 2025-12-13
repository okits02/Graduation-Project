package com.example.product_service.service;

import com.okits02.common_lib.dto.PageResponse;
import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    public PageResponse<CategoryResponse> finAll(int Page, int Size);
    public CategoryResponse findById(String categoryId);
    public Category createCate(CategoryRequest request);
    public CategoryResponse updateCate(CategoryRequest request);
    public List<String> getCategoryHierarchy(Set<String> categoryId);
    public void deleteCateById(String categoryId);
}
