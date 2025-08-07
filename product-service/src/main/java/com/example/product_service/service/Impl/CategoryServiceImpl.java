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
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
        if (StringUtils.hasText(newCategory.getParentId())) {
            Optional<Category> parentCateOptional = categoryRepository.findById(newCategory.getParentId());
            if (parentCateOptional.isPresent()) {
                Category parentCate = parentCateOptional.get();
                Set<String> childCate = parentCate.getChildrenId();
                if (childCate == null) {
                    childCate = new HashSet<>();
                }
                childCate.add(newCategory.getId());
                parentCate.setChildrenId(childCate);
                categoryRepository.save(parentCate);
            } else {
                // Optional: Handle case where parentId is invalid (e.g., throw custom exception)
                throw new IllegalArgumentException("Parent category with ID " + newCategory.getParentId() + " not found");
            }
        }

        log.info("category: {}", newCategory);
        return newCategory;
    }

    @Override
    public CategoryResponse updateCate(CategoryRequest request) {
        Category category = categoryRepository.findByName(request.getName());
        if(category != null){
            throw new AppException(ErrorCode.CATE_EXISTS);
        }
        categoryMapper.updateCategory(category, request);
        category.setParentId(request.getParentId());
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getCategoryHierarchy(Set<String> categoryId) {
        List<CategoryResponse> categoryHierarchy = new ArrayList<>();
        Set<String> categoryList = categoryId;
        if (categoryList!=null && !categoryList.isEmpty())
        {
            for(String id : categoryList) {
                Optional<Category> categoryOptional = categoryRepository.findById(id);
                categoryHierarchy.add(categoryMapper.toCategoryResponse(categoryOptional.get()));
                if(categoryOptional.get().getParentId() != null){
                    String parentId = categoryOptional.get().getParentId();
                    while (parentId != null && !parentId.isEmpty()){
                        Optional<Category> categoryParent = categoryRepository.findById(parentId);
                        if(categoryParent.isPresent()){
                            categoryHierarchy.add(categoryMapper.toCategoryResponse(categoryParent.get()));
                            parentId = categoryParent.get().getParentId();
                        }else {
                            break;
                        }
                    }
                }
            }
        }
        return categoryHierarchy;
    }

    @Override
    public void deleteCateById(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
