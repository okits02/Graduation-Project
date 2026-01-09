package com.example.order_service.kafka;

import com.example.order_service.enums.Status;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderAnalysisEvent {
    String id;
    String orderId;
    String userId;
    Status orderStatus;
    BigDecimal orderFee;
    BigDecimal totalPrice;
    LocalDateTime orderDate;
    List<OrderItemEvent> items;
}
