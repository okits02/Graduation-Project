package com.okits02.payment_service.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.payment_service.dto.response.HistoryPaymentInfoResponse;
import com.okits02.payment_service.enums.PaymentMethod;
import com.okits02.payment_service.enums.PaymentStatus;
import com.okits02.payment_service.exception.PaymentErrorCode;
import com.okits02.payment_service.model.Payment;
import com.okits02.payment_service.repository.PaymentRepository;
import com.okits02.payment_service.repository.httpClient.UserClient;
import com.okits02.payment_service.service.PaymentGatewayService;
import com.okits02.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final UserClient userClient;


    @Override
    public Object createPayment(String orderId, BigDecimal Amount, PaymentMethod paymentMethod)
            throws UnsupportedEncodingException, JsonProcessingException {
        Payment newPayment = Payment.builder()
                .orderId(orderId)
                .userId(getUserId())
                .amount(Amount)
                .method(paymentMethod)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(newPayment);
        Object provideResponse = null;
        switch (paymentMethod){
            case VNPAY -> provideResponse = paymentGatewayService.createVnPayPayment(newPayment);
        }
        return provideResponse;
    }

    @Override
    public Object refundPayment(String paymentId, PaymentMethod paymentMethod)
            throws JsonProcessingException {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
                new AppException(PaymentErrorCode.PAYMENT_NOT_EXISTS));
        if(payment.getStatus() != PaymentStatus.SUCCESS){
            return null;
        }
        Object refundResponse = null;
        switch (paymentMethod){
            case VNPAY -> refundResponse = paymentGatewayService.refundVnPay(payment, "hoan tien don hang");
        }
        return refundResponse;
    }

    @Override
    public PageResponse<HistoryPaymentInfoResponse> getHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String userId = getUserId();
        var pageData = paymentRepository.findAllByUserId(userId, pageable);

        List<HistoryPaymentInfoResponse> list = pageData.getContent().stream()
                .map(payment -> HistoryPaymentInfoResponse.builder()
                        .id(payment.getId())
                        .amount(payment.getAmount())
                        .status(payment.getStatus())
                        .createAt(payment.getCreatedAt())
                        .orderId(payment.getOrderId())
                        .build()
                ).toList();
        return PageResponse.<HistoryPaymentInfoResponse>builder()
                .currentPage(page)
                .totalElements(pageData.getTotalElements())
                .totalPage(pageData.getTotalPages())
                .data(list)
                .build();
    }


    private String getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult().getUserId();
    }
}
