package com.okits02.inventory_service.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    String variantName;
    String sku;
    String color;
    BigDecimal price;
    Integer sold;
    String thumbnail;
    Boolean inStock;
    LocalDate createAt;
    LocalDate updateAt;
}
