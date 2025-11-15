package com.example.search_service.viewmodel;

import com.example.search_service.model.Products;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductAdminGetVM {
    String id;
    String name;
    String thumbnailUrl;
    public static ProductAdminGetVM fromEntity(Products products){
        return ProductAdminGetVM.builder()
                .id(products.getId())
                .name(products.getName())
                .thumbnailUrl(products.getThumbnail())
                .build();
    }
}
