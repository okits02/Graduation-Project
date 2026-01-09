package com.okits02.delivery_serivce.dto.response;

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
    String sku;
    String productName;
    String thumbnailUrl;
    Integer quantity;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    LocalDateTime addAt;
}
