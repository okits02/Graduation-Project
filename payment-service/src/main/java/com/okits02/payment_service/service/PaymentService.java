package com.okits02.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.payment_service.dto.request.PaymentCreationRequest;

import java.io.UnsupportedEncodingException;

public interface PaymentService {
    public Object createPayment(PaymentCreationRequest request) throws UnsupportedEncodingException, JsonProcessingException;
}
