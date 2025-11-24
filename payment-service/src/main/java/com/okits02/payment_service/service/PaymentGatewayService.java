package com.okits02.payment_service.service;

import com.okits02.payment_service.dto.request.VnPayPaymentInfoRequest;
import com.okits02.payment_service.model.Payment;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface PaymentGatewayService {
    public ResponseEntity<?> createVnPayPayment(Payment payment) throws UnsupportedEncodingException;
}
