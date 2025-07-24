package com.example.search_service.viewmodel;

import com.example.search_service.model.Products;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductNameGetVm {
    private String name;
    public static ProductNameGetVm fromModel(Products products)
    {
        return new ProductNameGetVm(products.getName());
    }
}
