package com.okits02.payment_service.service;

import com.okits02.payment_service.model.Payment;
import org.springframework.http.ResponseEntity;

public interface PaymentGatewayService {
    public ResponseEntity<?> createVnpayPayment(Payment payment);
}
