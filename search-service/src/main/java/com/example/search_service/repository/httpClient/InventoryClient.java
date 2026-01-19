package com.example.search_service.repository.httpClient;

import com.example.search_service.viewmodel.dto.response.InventoryResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping(value = "/inventory-service/inventory/internal/quantity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<List<InventoryResponse>> getListQuantity(
            @RequestParam(value = "skus") List<String> skus
    );
}
