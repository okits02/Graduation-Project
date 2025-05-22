package com.example.product_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    String id;
    String name;
    String description;
    double listPrice;
    double sellPrice;
    Integer quantity;
    Integer sold;
    Float discount;
    Map<String, String> specifications;
    String categoryId;
    List<String> imagesToDelete;
}
