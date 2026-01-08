package com.example.order_service.repository.httpClient;

import com.example.order_service.dto.request.InventoryAdjustmentRequest;
import com.example.order_service.dto.request.IsInStockRequest;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @PostMapping(value = "/inventory-service/inventory/internal/check-inStock",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Boolean> checkInStock(
            @RequestBody IsInStockRequest request
    );

    @PostMapping(value = "/inventory-service/inventory/internal/decrease-stock",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> decreaseStock(
            @RequestBody InventoryAdjustmentRequest request
    );

    @PostMapping(value = "/inventory-service/inventory/internal/increase-stock",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> increaseStock(
            @RequestBody InventoryAdjustmentRequest request
    );
}
