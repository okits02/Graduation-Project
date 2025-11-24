package com.okits02.payment_service.controller;

import com.okits02.payment_service.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class PaymentGatewayController {
    private final PaymentGatewayService paymentGatewayService;

}
