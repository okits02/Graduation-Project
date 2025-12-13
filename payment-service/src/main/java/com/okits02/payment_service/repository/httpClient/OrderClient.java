package com.okits02.payment_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.payment_service.dto.response.GetAmountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "order-service")
public interface OrderClient {
    @GetMapping(value = "/order-service/order/internal/getAmount", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<GetAmountResponse> getAmount(@RequestHeader("Authorization") String token,
                                             @RequestParam("orderId") String orderId);
}
