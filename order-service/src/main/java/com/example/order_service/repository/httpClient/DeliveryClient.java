package com.example.order_service.repository.httpClient;

import com.example.order_service.dto.request.OrderDeliveryRequest;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {
    @GetMapping(value = "/delivery-service/deliveries/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Object> createShipment(@RequestBody OrderDeliveryRequest orderRequest);

    @DeleteMapping(value = "/delivery-service/deliveries/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> deleteDelivery(
            @RequestParam(value = "orderId") String orderId
    );
}
