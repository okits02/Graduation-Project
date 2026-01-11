package com.okits02.payment_service.repository;


import com.okits02.payment_service.enums.PaymentStatus;
import com.okits02.payment_service.enums.TransactionType;
import com.okits02.payment_service.model.PaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentSessionRepository extends JpaRepository<PaymentSession, String> {
    PaymentSession findByTransactionId(String vnpTxnRef);

    PaymentSession findTopByPaymentIdAndTransactionTypeAndStatusOrderByCreateAtDesc(String id,
                                                                          TransactionType transactionType,
                                                                          PaymentStatus paymentStatus);
}
