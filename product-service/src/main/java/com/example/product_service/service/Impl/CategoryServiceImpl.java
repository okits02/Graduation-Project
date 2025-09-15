package com.example.product_service.service.Impl;

import com.example.product_service.dto.PageResponse;
import com.example.product_service.dto.RemoveCategoryRequest;
import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.exceptions.ErrorCode;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.model.Category;
import com.example.product_service.model.Products;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.httpsClient.SearchClient;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;
    private final SearchClient searchClient;
    @Override
    public PageResponse<CategoryResponse> finAll(int Page, int Size) {
        Pageable pageable = PageRequest.of(Page, Size);
        var pageData = categoryRepository.findAll(pageable);
        return PageResponse.<CategoryResponse>builder()
                .currentPage(Page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(categoryMapper::toCategoryResponse).toList())
                .build();
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
        Optional<Category> category = categoryRepository.findById(request.getId());
        if(category.isEmpty()){
            throw new AppException(ErrorCode.CATE_EXISTS);
        }
        categoryMapper.updateCategory(category.orElse(null), request);
        category.get().setParentId(request.getParentId());
        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(categoryRepository.save(category.get()));
        return categoryResponse;
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
        Category category = categoryRepository.findById(categoryId).orElseThrow(()
                -> new AppException(ErrorCode.CATE_NOT_EXISTS));
        List<String> allDescendants = getAllDescendantIds(category);
        if(category.getParentId() != null && !category.getParentId().isEmpty()){
            Category parent = categoryRepository.findById(category.getParentId()).orElseThrow(() ->
                    new AppException(ErrorCode.CATE_NOT_EXISTS));
            parent.getChildrenId().remove(categoryId);
            categoryRepository.save(parent);
        }
        for(String childId : allDescendants){
            categoryRepository.deleteById(childId);
            removeCateInProduct(childId);
        }
        removeCateInProduct(categoryId);
        allDescendants.add(categoryId);
        RemoveCategoryRequest request = RemoveCategoryRequest.builder()
                .categoryIds(allDescendants)
                .build();
        var response = searchClient.removeCate(request);
        ApiResponse<Long> apiResponse = response.getBody();
        if(apiResponse.getCode() == 200){
            log.info(apiResponse.getMessage());
        }else {
            log.info("Remove cate on search failed!");
        }
        categoryRepository.deleteById(categoryId);
    }
    private List<String> getAllDescendantIds(Category category){
        List<String> result = new ArrayList<>();
        if(category.getChildrenId() != null || !category.getChildrenId().isEmpty())
        {
        for(String childId : category.getChildrenId()){
            Category child = categoryRepository.findById(childId).orElseThrow(() ->
                    new AppException(ErrorCode.CATE_NOT_EXISTS));
            result.addAll(getAllDescendantIds(child));
            result.add(childId);
        }
        }
        return result;
    }
    private void removeCateInProduct(String CateIds){
        List<Products> productsList = productRepository.findByCategoryId(CateIds);
        for(var products : productsList){
            products.getCategoryId().remove(CateIds);
            productRepository.save(products);
        }
    }
}
