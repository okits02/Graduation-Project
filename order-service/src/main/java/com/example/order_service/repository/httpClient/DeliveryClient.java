package com.example.order_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {

}
