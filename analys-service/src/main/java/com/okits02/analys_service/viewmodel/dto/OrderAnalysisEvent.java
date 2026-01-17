package com.okits02.analys_service.viewmodel.dto;

import com.okits02.analys_service.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
