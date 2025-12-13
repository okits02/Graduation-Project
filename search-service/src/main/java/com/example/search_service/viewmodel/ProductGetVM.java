package com.example.search_service.viewmodel;

import com.example.search_service.model.Category;
import com.example.search_service.model.Products;
import com.example.search_service.model.Specification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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
    String color;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal listPrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal sellPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    Float discount;
    String thumbnailUrl;
    List<String> imageList;
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
                .listPrice(products.getListPrice())
                .sellPrice(products.getSellPrice())
                .quantity(products.getQuantity())
                .avgRating(products.getAvgRating())
                .thumbnailUrl(products.getThumbnail())
                .sold(products.getSold())
                .categories(products.getCategoriesId().stream()
                        .map(categoryMap::get)
                        .filter(Objects::nonNull)
                        .toList())
                .specifications(products.getSpecifications())
                .createAt(products.getCreateAt())
                .updateAt(products.getUpdateAt())
                .build();
    }
}
