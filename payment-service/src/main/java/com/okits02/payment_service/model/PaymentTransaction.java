package com.okits02.payment_service.model;

import com.okits02.payment_service.enums.PaymentMethod;
import com.okits02.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String orderId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;

    private String transactionRef;
    private String providerResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
