package com.okits02.delivery_serivce.dto.response;

import com.okits02.delivery_serivce.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse implements Serializable {
    String orderId;
    LocalDateTime orderDate;
    String orderDesc;
    BigDecimal orderFee;
    Status orderStatus;
    String userId;
    String paymentId;
    String addressId;
    String deliveryId;
    BigDecimal totalPrice;
    List<OrderItemResponse> items;
}
