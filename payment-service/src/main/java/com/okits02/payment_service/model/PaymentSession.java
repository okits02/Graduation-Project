package com.okits02.payment_service.model;

import com.okits02.payment_service.enums.PaymentMethod;
import com.okits02.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    Payment payment;

    String transactionId;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String providerData;
    @Enumerated(EnumType.STRING)
    PaymentMethod method;
    @Enumerated(EnumType.STRING)
    PaymentStatus status;
    LocalDateTime createAt = LocalDateTime.now();
    LocalDateTime updateAt = LocalDateTime.now();
}
