package com.example.search_service.viewmodel;

import com.example.search_service.model.ProductVariants;
import com.example.search_service.model.Products;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetListSkuVM {
    List<String> skus;
    public static GetListSkuVM fromEntity(Products products){
        return GetListSkuVM.builder()
                .skus(products.getProductVariants().stream().map(ProductVariants::getSku).toList())
                .build();
    }
}
