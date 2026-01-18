package com.example.product_service.repository.httpsClient;

import com.example.product_service.dto.RemoveCategoryRequest;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "search-service")
public interface SearchClient {
    @PutMapping(value = "/search-service/search/internal/categories/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Long>> removeCate(
            @RequestBody RemoveCategoryRequest request
            );

    @PutMapping(value = "/search-service/search/internal/stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> changStock(
            @RequestParam(value = "sku") String sku,
            @RequestParam(value = "isStock") Boolean isStock
    );
}