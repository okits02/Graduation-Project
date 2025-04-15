package com.example.product_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRequest {
    String keyword;
    String categoryId;
    String brand;

    BigDecimal priceFrom;
    BigDecimal priceTo;

    Map<String, String> attribute;

    String sortBy;
    String sortDirection;

    Integer page = 0;
    Integer size = 10;
}
