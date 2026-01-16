package com.okits02.delivery_serivce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    String orderId;
    String addressId;
    String userId;
    BigDecimal totalCost;
    List<OrderItemRequest> items;
}
