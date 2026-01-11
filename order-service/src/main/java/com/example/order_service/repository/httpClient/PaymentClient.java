package com.example.order_service.repository.httpClient;

import com.example.order_service.enums.PaymentMethod;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "payment-service")
public interface PaymentClient {
    @GetMapping(value = "/payment-service/pay/create", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<?>> createPayment(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "amount") BigDecimal amount,
            @RequestParam(value = "PaymentMethod")PaymentMethod paymentMethod);

    @GetMapping(value = "payment-service/pay/refund", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> refundPayment(
            @RequestHeader("Authorization") String token,
            @RequestParam("paymentId") String paymentId
    );
}
