package com.example.product_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    String thumbNail;
    List<String> imageList;
    Map<String, String> specifications;
    Set<String> categoryId;
}
