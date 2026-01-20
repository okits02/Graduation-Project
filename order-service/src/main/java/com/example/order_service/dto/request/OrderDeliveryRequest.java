package com.example.order_service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDeliveryRequest {
    String orderId;
    String addressId;
    String userId;
    BigDecimal totalCost;
    BigDecimal orderFee;
    List<ItemDeliveryRequest> items;
}
