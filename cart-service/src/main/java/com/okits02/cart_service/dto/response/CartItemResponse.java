package com.okits02.cart_service.dto.response;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    String cartItemId;
    String sku;
    String variantName;
    String thumbnail;
    Integer quantity;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    List<String> promotionName;
    LocalDateTime addedAt;
}
