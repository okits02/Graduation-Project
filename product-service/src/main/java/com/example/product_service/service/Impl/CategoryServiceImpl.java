package com.example.product_service.service.Impl;

import com.example.product_service.dto.response.CategoryLevelValidateResponse;
import com.example.product_service.kafka.CategoryEvent;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.product_service.dto.RemoveCategoryRequest;
import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.product_service.exceptions.ProductErrorCode;
import com.example.product_service.helper.CategoryMappingHelper;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.model.Category;
import com.example.product_service.model.Products;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.httpsClient.SearchClient;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;
    private final SearchClient searchClient;
    private final CategoryMappingHelper categoryMappingHelper;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public PageResponse<CategoryResponse> finAll(int Page, int Size) {
        Pageable pageable = PageRequest.of(Page, Size);
        var pageData = categoryRepository.findAll(pageable);
        return PageResponse.<CategoryResponse>builder()
                .currentPage(Page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(categoryMappingHelper::map).toList())
                .build();
    }

    @Override
    public CategoryResponse findById(String categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()->
                new AppException(ProductErrorCode.CATE_NOT_EXISTS));
        return categoryMappingHelper.map(category);
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
        sendKafKaEvent(newCategory, "CATEGORY_CREATED");
        log.info("category: {}", newCategory);
        return newCategory;
    }

    @Override
    public CategoryResponse updateCate(CategoryRequest request) {
        Optional<Category> category = categoryRepository.findById(request.getId());
        if(category.isEmpty()){
            throw new AppException(ProductErrorCode.CATE_EXISTS);
        }
        categoryMapper.updateCategory(category.orElse(null), request);
        category.get().setParentId(request.getParentId());
        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(categoryRepository.save(category.get()));
        sendKafKaEvent(category.get(), "CATEGORY_UPDATED");
        return categoryResponse;
    }

    @Override
    public List<String> getCategoryHierarchy(Set<String> categoryId) {
        List<String> categoryHierarchy = new ArrayList<>();
        Set<String> categoryList = categoryId;
        if (categoryList!=null && !categoryList.isEmpty())
        {
            for(String id : categoryList) {
                Optional<Category> categoryOptional = categoryRepository.findById(id);
                categoryHierarchy.add(categoryOptional.get().getId());
                if(categoryOptional.get().getParentId() != null){
                    String parentId = categoryOptional.get().getParentId();
                    while (parentId != null && !parentId.isEmpty()){
                        Optional<Category> categoryParent = categoryRepository.findById(parentId);
                        if(categoryParent.isPresent()){
                            categoryHierarchy.add(categoryParent.get().getId());
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
                -> new AppException(ProductErrorCode.CATE_NOT_EXISTS));
        List<String> allDescendants = getAllDescendantIds(category);
        if(category.getParentId() != null && !category.getParentId().isEmpty()){
            Category parent = categoryRepository.findById(category.getParentId()).orElseThrow(() ->
                    new AppException(ProductErrorCode.CATE_NOT_EXISTS));
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
        sendKafKaEvent(category, "CATEGORY_DELETED");
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public CategoryLevelValidateResponse validateSameLevel(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.size() <= 1) {
            return CategoryLevelValidateResponse.builder().valid(true).build();
        }
        List<Category> selectedCategories = categoryRepository.findAllById(categoryIds);
        List<Category> allCategories = categoryRepository.findAll();
        Map<String, String> parentMap = new HashMap<>();
        for (Category c : allCategories) {
            parentMap.put(c.getId(), c.getParentId());
        }

        Set<String> selectedIds = new HashSet<>(categoryIds);

        for (String categoryId : selectedIds) {
            String parentId = parentMap.get(categoryId);

            while (parentId != null && !parentId.isBlank()) {
                if (selectedIds.contains(parentId)) {
                    return CategoryLevelValidateResponse.builder()
                            .valid(false)
                            .build();
                }
                parentId = parentMap.get(parentId);
            }
        }

        return CategoryLevelValidateResponse.builder().valid(true).build();
    }


    private List<String> getAllDescendantIds(Category category){
        List<String> result = new ArrayList<>();
        if(category.getChildrenId() != null || !category.getChildrenId().isEmpty())
        {
        for(String childId : category.getChildrenId()){
            Category child = categoryRepository.findById(childId).orElseThrow(() ->
                    new AppException(ProductErrorCode.CATE_NOT_EXISTS));
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



    private void sendKafKaEvent(Category category, String eventType){
        switch (eventType){
            case "CATEGORY_CREATED" -> {
                CategoryEvent categoryEvent = CategoryEvent.builder()
                        .eventType("CATEGORY_CREATED")
                        .id(category.getId())
                        .name(category.getName())
                        .special(category.getSpecial())
                        .parentId(category.getParentId())
                        .build();
                kafkaTemplate.send("category-event", categoryEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
            case "CATEGORY_UPDATED" -> {
                List<String> children = category.getChildrenId() == null ? new ArrayList<>()
                        : new ArrayList<>(category.getChildrenId());
                CategoryEvent categoryEvent = CategoryEvent.builder()
                        .eventType("CATEGORY_UPDATE")
                        .id(category.getId())
                        .name(category.getName())
                        .parentId(category.getParentId())
                        .special(category.getSpecial())
                        .childrentId(children)
                        .build();
                kafkaTemplate.send("category-event", categoryEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
            case "CATEGORY_DELETED" -> {
                CategoryEvent categoryEvent = CategoryEvent.builder()
                        .eventType("CATEGORY_DELETED")
                        .id(category.getId())
                        .build();
                kafkaTemplate.send("category-event", categoryEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
        }
    }
}
