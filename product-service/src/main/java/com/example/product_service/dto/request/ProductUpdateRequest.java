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
    String color;
    Integer sold;
    String thumbNail;
    String videoUrl;
    List<String> imageList;
    Map<String, String> specifications;
    Set<String> categoryId;
}
