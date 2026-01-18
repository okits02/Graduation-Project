package com.example.order_service.dto.response;

import com.example.order_service.constant.AppConstant;
import com.example.order_service.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
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
    Object paymentUrl;
    String orderId;
    LocalDateTime orderDate;
    String orderDesc;
    BigDecimal orderFee;
    Status orderStatus;
    String userId;
    String paymentId;
    String addressId;
    String deliveryId;
    String voucherCode;
    BigDecimal totalPrice; // gia sau khi tru discount
    BigDecimal discount;
    BigDecimal beforePrice; // gia truoc khi tru discount
    List<OrderItemResponse> items;
}
