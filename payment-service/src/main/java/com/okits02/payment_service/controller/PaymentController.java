package com.okits02.payment_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.dto.response.HistoryPaymentInfoResponse;
import com.okits02.payment_service.enums.PaymentMethod;
import com.okits02.payment_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @GetMapping("/create")
    public ResponseEntity<ApiResponse<?>> createPayment(
            @RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "amount") BigDecimal amount,
            @RequestParam(value = "PaymentMethod") PaymentMethod paymentMethod)
            throws UnsupportedEncodingException, JsonProcessingException {
        return ResponseEntity.ok(ApiResponse.<Object>builder()
                        .code(200)
                        .message("create payment successfully!")
                        .result(paymentService.createPayment(orderId, amount, paymentMethod))
                .build());
    }

    @GetMapping("/vnp-return")
    public ResponseEntity<String> vnpayReturn(HttpServletRequest request) {
        request.getParameterMap().forEach((k, v) ->
                System.out.println(k + " = " + String.join(",", v)));

        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        String redirectUrl;

        if ("00".equals(vnp_ResponseCode)) {
            redirectUrl = UriComponentsBuilder
                    .fromHttpUrl(frontendBaseUrl)
                    .queryParam("orderId", vnp_TxnRef)
                    .queryParam("code", vnp_ResponseCode)
                    .build()
                    .toUriString();

        } else {
            redirectUrl = UriComponentsBuilder
                    .fromHttpUrl(frontendBaseUrl + "/checkout")
                    .queryParam("orderId", vnp_TxnRef)
                    .queryParam("code", vnp_ResponseCode)
                    .build()
                    .toUriString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<HistoryPaymentInfoResponse>> getHistoryPayment(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        return ApiResponse.<PageResponse<HistoryPaymentInfoResponse>>builder()
                .code(200)
                .message("get history successfully!")
                .result(paymentService.getHistory(page, size))
                .build();
    }

    @GetMapping("/refund")
    public ApiResponse<?> refundPayment(
        @RequestParam("paymentId") String paymentId
    ) throws JsonProcessingException {
        return ApiResponse.builder()
                .result(paymentService.refundPayment(paymentId, PaymentMethod.VNPAY))
                .build();
    }
}
