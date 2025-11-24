package com.okits02.payment_service.dto.response;

import com.okits02.payment_service.enums.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnpResponseDTO {
    String paymentUrl;
    String txnRef;
    BigDecimal amount;
    PaymentMethod method;
}
