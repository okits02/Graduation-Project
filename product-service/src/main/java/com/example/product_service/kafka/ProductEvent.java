package com.example.product_service.kafka;

import com.example.product_service.model.ProductVariants;
import com.example.product_service.model.Specifications;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEvent {
    String eventType;
    String id;
    String name;
    String brand;
    String description;
    Integer quantity;
    double avgRating;
    String videoUrl;
    List<String> categoriesId;
    List<Specifications> specifications;
    List<ProductVariants> productVariants;
    LocalDate createAt;
    LocalDate updateAt;
}
