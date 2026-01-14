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
    List<Specification> specifications;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate updateAt;

    public static ProductSummariseVM fromEntity(Products products){
        ProductVariants bestVariant = getBestSellingVariants(products);
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
                .price(bestVariant.getPrice() != null
                        ? bestVariant.getPrice().toString()
                        : null)
                .listPrice(bestVariant.getSellPrice() != null
                        ? bestVariant.getSellPrice().toString()
                        : null)
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

        Optional<ProductVariants> bestSoldVariant = products.getProductVariants()
                .stream()
                .filter(v -> v.getSold() != null)
                .max(Comparator.comparing(ProductVariants::getSold));

        return bestSoldVariant.orElse(products.getProductVariants().get(0));
    }
}
