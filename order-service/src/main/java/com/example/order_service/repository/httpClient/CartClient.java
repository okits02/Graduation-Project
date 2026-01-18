package com.example.order_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "cart-service")
public interface CartClient {

    //@PutMapping("/cart-service/cart/internal/remove")
}
