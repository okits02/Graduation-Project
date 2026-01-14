package com.example.order_service.repository.httpClient;

import com.example.order_service.dto.request.CheckValidVoucherRequest;
import com.example.order_service.dto.response.PromotionResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "promotion-service")
public interface PromotionClient {
    @PostMapping(value = "/promotion-service/promotion/internal/voucher/check",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PromotionResponse> checkValidPromotion(
            @RequestBody CheckValidVoucherRequest request,
            @RequestHeader("Authorization") String token
            );
    @PostMapping(value = "/promotion-service/promotion/internal/voucher/rollBack",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<?> rollBackPromotion(
            @RequestParam String orderId,
            @RequestHeader("Authorization") String token
    );
    @PostMapping(value = "/promotion-service/promotion/internal/voucher/applyForOrder",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<?> applyForOrder(
            @RequestParam String orderId,
            @RequestParam String voucherCode,
            @RequestHeader("Authorization") String token
    );
}
