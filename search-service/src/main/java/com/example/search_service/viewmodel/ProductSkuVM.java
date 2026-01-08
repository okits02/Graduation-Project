package com.example.search_service.viewmodel;

import com.example.search_service.model.ProductVariants;
import com.example.search_service.model.Products;
import com.example.search_service.model.Promotion;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSkuVM {
    String id;
    String sku;
    String variantName;
    String color;
    String thumbnailUrl;
    List<String> promotionName;
    List<String> categoriesId;
    BigDecimal sellPrice;
    BigDecimal listPrice;
    public static ProductSkuVM fromEntity(Products products, String sku){
        ProductVariants variant = products.getProductVariants()
                .stream()
                .filter(v -> sku.equals(v.getSku()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Variant not found for sku: " + sku)
                );
        List<String> promotionNames = products.getPromotions() == null
                ? List.of()
                : products.getPromotions().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(Promotion::getName)
                .toList();
        return ProductSkuVM.builder()
                .id(products.getId())
                .sku(variant.getSku())
                .variantName(variant.getVariantName())
                .color(variant.getColor())
                .thumbnailUrl(variant.getThumbnail())
                .listPrice(variant.getPrice())
                .sellPrice(variant.getSellPrice())
                .promotionName(promotionNames)
                .categoriesId(products.getCategoriesId())
                .build();
    }
}
