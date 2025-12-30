package com.example.order_service.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {
    String productId;
    String productName;
    String sku;
    String thumbnailUrl;
    Integer quantity;
    BigDecimal listPrice;
    BigDecimal sellPrice;
}
