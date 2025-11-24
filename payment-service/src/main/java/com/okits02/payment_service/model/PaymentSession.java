package com.okits02.payment_service.model;

import com.okits02.payment_service.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    Payment payment;

    String transactionId;
    String providerData;
    @Enumerated(EnumType.STRING)
    PaymentMethod method;
    LocalDateTime createAt = LocalDateTime.now();
}
