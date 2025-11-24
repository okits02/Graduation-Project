package com.okits02.payment_service.service.Impl;

import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.enums.PaymentStatus;
import com.okits02.payment_service.mapper.PaymentMapper;
import com.okits02.payment_service.model.Payment;
import com.okits02.payment_service.repository.httpClient.UserClient;
import com.okits02.payment_service.service.PaymentGatewayService;
import com.okits02.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentMapper paymentMapper;
    private final UserClient userClient;


    @Override
    public Object createPayment(PaymentCreationRequest request) throws UnsupportedEncodingException {
        Payment newPayment = paymentMapper.toPayment(request);
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setUserId(getUserId());
        Object provideResponse = null;
        switch (request.getMethod()){
            case VNPAY -> provideResponse = paymentGatewayService.createVnPayPayment(newPayment);
        }
        return provideResponse;
    }


    private String getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult().getUserId();
    }
}
