package com.example.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryAdjustmentRequest {
    String sku;
    Integer quantity;
    String orderId;
}
