package com.example.order_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSkuVM {
    String id;
    String sku;
    String variantName;
    String thumbnailUrl;
    List<String> promotionName;
    List<String> categoriesId;
    BigDecimal sellPrice;
    BigDecimal listPrice;
}
