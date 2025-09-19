package com.example.product_service.helper;


import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.model.Products;
import com.example.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMappingHelper {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    public ProductResponse map(final Products products) {
        var listCategory = products.getCategoryId().stream()
                .map(categoryRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(categoryMapper::toCategoryResponse).toList();
        return ProductResponse.builder()
                .id(products.getId())
                .name(products.getName())
                .listCategory(listCategory)
                .avgRating(products.getAvgRating())
                .color(products.getColor())
                .description(products.getDescription())
                .sold(products.getSold())
                .listPrice(products.getListPrice())
                .thumbNail(products.getThumbNail())
                .avgRating(products.getAvgRating())
                .imageList(products.getImageList())
                .quantity(products.getQuantity())
                .specifications(products.getSpecifications())
                .build();
    }
}
