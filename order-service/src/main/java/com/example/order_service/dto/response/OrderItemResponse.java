package com.example.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String productId;
    BigDecimal sellPrice;
    Double totalPromotion;
    BigDecimal listPrice;
    String thumbnailUrl;
    Integer quantity;
    BigDecimal totalPrice;
}
