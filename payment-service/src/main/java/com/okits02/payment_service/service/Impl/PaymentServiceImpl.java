package com.okits02.payment_service.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.dto.response.HistoryPaymentInfoResponse;
import com.okits02.payment_service.enums.PaymentStatus;
import com.okits02.payment_service.mapper.PaymentMapper;
import com.okits02.payment_service.model.Payment;
import com.okits02.payment_service.model.PaymentSession;
import com.okits02.payment_service.repository.PaymentRepository;
import com.okits02.payment_service.repository.PaymentSessionRepository;
import com.okits02.payment_service.repository.httpClient.OrderClient;
import com.okits02.payment_service.repository.httpClient.UserClient;
import com.okits02.payment_service.service.PaymentGatewayService;
import com.okits02.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentMapper paymentMapper;
    private final UserClient userClient;
    private final OrderClient orderClient;


    @Override
    public Object createPayment(PaymentCreationRequest request)
            throws UnsupportedEncodingException, JsonProcessingException {
        Payment newPayment = paymentMapper.toPayment(request);
        var amount = getAmount(request.getOrderId());
        newPayment.setAmount(amount);
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setUserId(getUserId());
        paymentRepository.save(newPayment);
        Object provideResponse = null;
        switch (request.getMethod()){
            case VNPAY -> provideResponse = paymentGatewayService.createVnPayPayment(newPayment);
        }
        return provideResponse;
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
    private BigDecimal getAmount(String orderId){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var response = orderClient.getAmount(authHeader, orderId);
        return response.getResult().getAmount();
    }
}
