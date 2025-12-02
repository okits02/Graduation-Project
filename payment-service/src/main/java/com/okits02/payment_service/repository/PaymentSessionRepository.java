package com.okits02.payment_service.repository;


import com.okits02.payment_service.model.PaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentSessionRepository extends JpaRepository<PaymentSession, String> {
    PaymentSession findByTransactionId(String vnpTxnRef);
}
