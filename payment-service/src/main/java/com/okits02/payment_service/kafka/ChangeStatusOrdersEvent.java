package com.okits02.payment_service.kafka;

import com.okits02.payment_service.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangeStatusOrdersEvent {
    String orderId;
    String paymentId;
    PaymentStatus status;
}
