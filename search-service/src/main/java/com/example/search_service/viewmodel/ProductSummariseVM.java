package com.example.search_service.viewmodel;

import com.example.search_service.model.ProductVariants;
import com.example.search_service.model.Products;
import com.example.search_service.model.Specification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSummariseVM {
    String id;
    String name;
    String variantName;
    String thumbnailUrl;
    String price;
    String listPrice;
    Double avgRating;
    Double discountPercent;
    Boolean isStock;
    Long sold;
    List<Specification> specifications;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate updateAt;

    public static ProductSummariseVM fromEntity(Products products){
        ProductVariants bestVariant = getBestSellingVariants(products);
        Boolean inStock = isProductInStock(products);
        if (bestVariant == null) {
            return ProductSummariseVM.builder()
                    .id(products.getId())
                    .name(products.getName())
                    .createAt(products.getCreateAt())
                    .updateAt(products.getUpdateAt())
                    .build();
        }
        return ProductSummariseVM.builder()
                .id(products.getId())
                .name(products.getName())
                .variantName(bestVariant.getVariantName())
                .thumbnailUrl(bestVariant.getThumbnail())
                .sold(products.getSold())
                .price(bestVariant.getSellPrice() != null
                        ? bestVariant.getSellPrice().toString()
                        : null)
                .listPrice(bestVariant.getPrice() != null
                        ? bestVariant.getPrice().toString()
                        : null)
                .isStock(inStock)
                .specifications(products.getSpecifications())
                .avgRating(products.getAvgRating())
                .createAt(products.getCreateAt())
                .updateAt(products.getUpdateAt())
                .build();
    }

    private static ProductVariants getBestSellingVariants(Products products){
        if (products.getProductVariants() == null || products.getProductVariants().isEmpty()) {
            return null;
        }

        return products.getProductVariants().stream()
                .filter(v ->
                        v.getSold() != null &&
                                v.getThumbnail() != null &&
                                !v.getThumbnail().isBlank()
                )
                .max(Comparator.comparing(ProductVariants::getSold))
                .orElseGet(() -> products.getProductVariants().stream()
                        .filter(v -> v.getThumbnail() != null && !v.getThumbnail().isBlank())
                        .findFirst()
                        .orElse(null)
                );
    }

    private static Boolean isProductInStock(Products products) {
        if (products.getProductVariants() == null || products.getProductVariants().isEmpty()) {
            return false;
        }
        return products.getProductVariants()
                .stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getInStock()));
    }
}
