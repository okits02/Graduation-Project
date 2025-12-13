package com.okits02.payment_service.dto.response;

import com.okits02.payment_service.enums.PaymentMethod;
import com.okits02.payment_service.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryPaymentInfoResponse {
    String id;
    String orderId;
    PaymentMethod method;
    PaymentStatus status;
    BigDecimal amount;
    LocalDateTime createAt;
}
