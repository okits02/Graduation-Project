package com.example.order_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "cart-service")
public interface CartClient {

    @PutMapping(value = "/cart-service/cart/internal/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> returnItem(
            @RequestParam List<String> skus,
            @RequestParam String userId
    );
}
