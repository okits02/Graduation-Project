package com.okits02.payment_service.controller;

import com.okits02.payment_service.service.PaymentGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class PaymentGatewayController {
    private final PaymentGatewayService paymentGatewayService;

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<?> vnPayIpn(HttpServletRequest request) throws UnsupportedEncodingException {
        return paymentGatewayService.vnPayIPN(request);
    }
}
