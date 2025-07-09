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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Category createCate(CategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        Category newCategory = categoryRepository.save(category);
        log.info("category: {}", newCategory);
        return newCategory;
    }

    @Override
    public CategoryResponse updateCate(CategoryRequest request) {
        Category category = categoryRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(ErrorCode.CATE_NOT_EXISTS));
        categoryMapper.updateCategory(category, request);
        category.setParentId(request.getParentId());
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<String> getCategoryHierarchy(String categoryId) {
        List<String> categoryHierarchy = new ArrayList<>();
        String currentId = categoryId;
        while (currentId!=null && !currentId.isEmpty())
        {
            Optional<Category> categoryOptional = categoryRepository.findById(currentId);
            if(categoryOptional.isPresent()) {
                categoryHierarchy.add(categoryOptional.get().getId());
                currentId = categoryOptional.get().getParentId();
            }else {
                break;
            }
        }
        return categoryHierarchy;
    }

    @Override
    public void deleteCateById(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
