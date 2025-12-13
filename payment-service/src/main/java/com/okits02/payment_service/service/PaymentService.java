package com.okits02.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.dto.response.HistoryPaymentInfoResponse;

import java.io.UnsupportedEncodingException;

public interface PaymentService {
    public Object createPayment(PaymentCreationRequest request)
            throws UnsupportedEncodingException, JsonProcessingException;
    public PageResponse<HistoryPaymentInfoResponse> getHistory(int page, int size);
}
