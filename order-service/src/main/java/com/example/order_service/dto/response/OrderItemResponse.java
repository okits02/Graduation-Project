package com.example.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String productId;
    String sku;
    String productName;
    Integer quantity;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    LocalDateTime addAt;
}
