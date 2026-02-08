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
    String sku;
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
                .sku(bestVariant.getSku())
                .price(bestVariant.getSellPrice() != null
                        ? bestVariant.getSellPrice().toString()
                        : null)
                .listPrice(bestVariant.getPrice() != null
                        ? bestVariant.getPrice().toString()
                        : null)
                .isStock(inStock)
                .specifications(products.getSpecifications())
                .discountPercent(calculateDiscountPercent(bestVariant))
                .avgRating(products.getAvgRating())
                .createAt(products.getCreateAt())
                .updateAt(products.getUpdateAt())
                .build();
    }

    private static ProductVariants getBestSellingVariants(Products products){
        if (products.getProductVariants() == null || products.getProductVariants().isEmpty()) {
            return null;
        }

        Optional<ProductVariants> withSoldAndThumbnail = products.getProductVariants().stream()
                .filter(v ->
                        v.getSold() != null &&
                                v.getThumbnail() != null &&
                                !v.getThumbnail().isBlank()
                )
                .max(Comparator.comparing(ProductVariants::getSold));

        if (withSoldAndThumbnail.isPresent()) {
            return withSoldAndThumbnail.get();
        }

        Optional<ProductVariants> withThumbnail = products.getProductVariants().stream()
                .filter(v -> v.getThumbnail() != null && !v.getThumbnail().isBlank())
                .findFirst();

        if (withThumbnail.isPresent()) {
            return withThumbnail.get();
        }

        return products.getProductVariants().stream().findFirst().orElse(null);
    }

    private static Boolean isProductInStock(Products products) {
        if (products.getProductVariants() == null || products.getProductVariants().isEmpty()) {
            return false;
        }
        return products.getProductVariants()
                .stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getInStock()));
    }

    private static Double calculateDiscountPercent(ProductVariants variant) {
        if (variant.getPrice() == null || variant.getSellPrice() == null) {
            return null;
        }

        if (variant.getPrice().compareTo(variant.getSellPrice()) == 0) {
            return null;
        }

        if (variant.getPrice().doubleValue() == 0) {
            return null;
        }

        double discount = (variant.getPrice().doubleValue() - variant.getSellPrice().doubleValue())
                / variant.getPrice().doubleValue() * 100;

        return Math.round(discount * 100.0) / 100.0;
    }

}
