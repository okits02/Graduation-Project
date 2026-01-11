package com.okits02.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.payment_service.dto.request.VnPayPaymentInfoRequest;
import com.okits02.payment_service.model.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayService {
    public ResponseEntity<?> createVnPayPayment(Payment payment)
            throws UnsupportedEncodingException, JsonProcessingException;
    public ResponseEntity<?> refundVnPay(Payment payment, String reason) throws JsonProcessingException;
    public ResponseEntity<?> vnPayIPN(HttpServletRequest request) throws UnsupportedEncodingException;


}
