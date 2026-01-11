package com.okits02.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.dto.response.HistoryPaymentInfoResponse;
import com.okits02.payment_service.enums.PaymentMethod;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

public interface PaymentService {
    public Object createPayment(String orderId, BigDecimal Amount, PaymentMethod paymentMethod)
            throws UnsupportedEncodingException, JsonProcessingException;
    public Object refundPayment(String paymentId, PaymentMethod paymentMethod) throws JsonProcessingException;
    public PageResponse<HistoryPaymentInfoResponse> getHistory(int page, int size);
}
