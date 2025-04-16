package com.example.product_service.service.Impl;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.exceptions.ErrorCode;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.model.Category;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    @Override
    public Page<CategoryResponse> finAll(int Page, int Size) {
        Pageable pageable = PageRequest.of(Page, Size);
        return categoryRepository.findAll(pageable).map(categoryMapper::toCategoryResponse);
    }

    @Override
    public CategoryResponse findById(String categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()->
                new AppException(ErrorCode.CATE_NOT_EXISTS));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse createCate(CategoryRequest request) {
        categoryRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(ErrorCode.CATE_EXISTS));
        Category category = categoryMapper.toCategory(request);
        categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse updateCate(CategoryRequest request) {
        Category category = categoryRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(ErrorCode.CATE_NOT_EXISTS));
        categoryMapper.updateCategory(category, request);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public void deleteCateById(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
