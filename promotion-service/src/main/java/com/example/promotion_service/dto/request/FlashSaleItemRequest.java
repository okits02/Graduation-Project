package com.example.promotion_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashSaleItemRequest {
    String productId;
    double discountPercent;
    double fixedAmount;
}
