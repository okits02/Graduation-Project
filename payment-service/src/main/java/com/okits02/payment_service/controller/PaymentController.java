package com.okits02.payment_service.controller;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createPayment(
            @RequestBody PaymentCreationRequest request) throws UnsupportedEncodingException {
        return ResponseEntity.ok(ApiResponse.<Object>builder()
                        .code(200)
                        .message("create payment successfully!")
                        .result(paymentService.createPayment(request))
                .build());
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(HttpServletRequest request) {
        // In ra để debug trước
        request.getParameterMap().forEach((k, v) ->
                System.out.println(k + " = " + String.join(",", v)));

        // Lấy mã phản hồi
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        if ("00".equals(vnp_ResponseCode)) {
            // Thanh toán thành công → xử lý đơn hàng ở đây
            System.out.println("Thanh toán THÀNH CÔNG - TxnRef: " + vnp_TxnRef);
            return ResponseEntity.ok("Thanh toán thành công! Mã đơn hàng: " + vnp_TxnRef);
        } else {
            System.out.println("Thanh toán THẤT BẠI - TxnRef: " + vnp_TxnRef + " - Code: " + vnp_ResponseCode);
            return ResponseEntity.ok("Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);
        }
    }
}
