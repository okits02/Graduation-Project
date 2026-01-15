package com.okits02.delivery_serivce.model;

import com.okits02.delivery_serivce.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String orderId;
    String userId;
    String addressId;
    String ghtkOrderCode;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private BigDecimal shippingFee;
    private String expectedDeliveryTime;

    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
