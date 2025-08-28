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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailsVM {
    String id;
    String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal listPrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal sellPrice;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate updateAt;

    public static ProductDetailsVM fromEntity(Products products){
        return ProductDetailsVM.builder()
                .id(products.getId())
                .name(products.getName())
                .listPrice(products.getListPrice())
                .sellPrice(products.getSellPrice())
                .createAt(products.getCreateAt())
                .updateAt(products.getUpdateAt())
                .build();
    }
}

