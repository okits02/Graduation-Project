package com.example.search_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping(value = "/inventory-service/inventory/internal/sold-by-skus",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Map<String, Long>> getSoldBySkus(
            @RequestParam("skus") List<String> skus
    );
}
