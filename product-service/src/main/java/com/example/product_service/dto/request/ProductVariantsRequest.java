package com.example.product_service.dto.request;

import com.example.product_service.enums.VariantAction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantsRequest {
    VariantAction action;
    String sku;
    String variantName;
    String color;
    BigDecimal price;
}
