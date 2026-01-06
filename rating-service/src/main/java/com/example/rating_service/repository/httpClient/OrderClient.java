package com.example.rating_service.repository.httpClient;

import com.example.rating_service.dto.response.CheckVerifiedPurchase;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.print.attribute.standard.Media;

@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping(value = "/order-serivce/order/internal/rating/check",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CheckVerifiedPurchase> checkVerifiedPurchase(
            @RequestParam("userId") String userId,
            @RequestParam("productId") String productId
    );
}
