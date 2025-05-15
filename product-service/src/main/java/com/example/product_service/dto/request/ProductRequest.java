package com.example.product_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String id;
    String name;
    String brand;
    String model;
    String description;
    BigDecimal price;
    Integer quantity;
    String imageUrl;
    Integer sold;
    String thumbnailUrl;
    String warrantyPeriod;
    Float discount;
    String categoryId;
}
