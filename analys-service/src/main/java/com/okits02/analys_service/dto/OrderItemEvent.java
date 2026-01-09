package com.okits02.analys_service.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemEvent {
    String orderItemId;
    String orderId;
    String sku;
    Integer quantity;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    LocalDateTime addAt;
}
