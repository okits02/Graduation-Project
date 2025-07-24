package com.example.product_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ProductRequest {
    String name;
    String description;
    BigDecimal listPrice;
    Integer quantity;
    Integer sold;
    double avgRating;
    String thumbNail;
    List<String> imageList;
    String categoryId;
    Map<String, String> specifications;
}
