package com.example.product_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantsRequest {
    String sku;
    String variants_name;
    String color;
    BigDecimal price;
    List<SpecificationRequest> bestSpecifications;
}
