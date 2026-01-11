package com.example.order_service.dto.request;

import com.example.order_service.enums.PaymentMethod;
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
public class RePaymentForOrder {
    String orderId;
    BigDecimal orderFee;
    String addressId;
    String voucher;
    PaymentMethod paymentMethod;
    List<OrderItemRequest> items;
}
