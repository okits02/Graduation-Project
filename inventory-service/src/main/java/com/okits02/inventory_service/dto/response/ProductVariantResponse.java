package com.okits02.inventory_service.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    String id;
    String sku;
    String variantName;
    String thumbnailUrl;
    List<String> promotionName;
    BigDecimal sellPrice;
    BigDecimal listPrice;
}
