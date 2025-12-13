package com.example.order_service.dto;

import com.example.order_service.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangeStatusOrderDTO {
    String orderId;
    String paymentId;
    PaymentStatus status;
}
