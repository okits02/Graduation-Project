package com.example.search_service.viewmodel;

import com.example.search_service.model.ProductVariants;
import com.example.search_service.model.Products;
import com.example.search_service.model.Specification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductGetVM {
    String id;
    String name;
    String description;
    double avgRating;
    Integer sold;
    String videoUrl;
    String thumbnailUrl;
    List<String> imageList;
    List<ProductVariants> variants;
    List<CategoryGetVM> categories;
    List<Specification> specifications;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate updateAt;

    public static ProductGetVM fromEntity(Products products,  Map<String, CategoryGetVM > categoryMap){
        return ProductGetVM.builder()
                .id(products.getId())
                .name(products.getName())
                .description(products.getDescription())
                .avgRating(products.getAvgRating())
                .categories(products.getCategoriesId().stream()
                        .map(categoryMap::get)
                        .filter(Objects::nonNull)
                        .toList())
                .videoUrl(products.getVideoUrl())
                .variants(products.getProductVariants())
                .specifications(products.getSpecifications())
                .createAt(products.getCreateAt())
                .updateAt(products.getUpdateAt())
                .build();
    }
}
