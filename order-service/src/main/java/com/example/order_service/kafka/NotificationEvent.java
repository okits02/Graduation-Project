package com.example.order_service.kafka;

import com.example.order_service.enums.Status;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String userId;
    List<String> skus;
    BigDecimal totalPrice;
    Status status;
}
