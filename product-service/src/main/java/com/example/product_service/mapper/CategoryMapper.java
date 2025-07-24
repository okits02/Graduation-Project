package com.example.product_service.mapper;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest request);
    CategoryResponse toCategoryResponse(Category category);
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
