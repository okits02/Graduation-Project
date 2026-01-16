package com.example.notification_service.dto;

import com.example.notification_service.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String userId;
    List<String> skus;
    BigDecimal totalPrice;
    String status;
}
