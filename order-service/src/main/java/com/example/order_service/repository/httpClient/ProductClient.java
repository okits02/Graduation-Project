package com.example.order_service.repository.httpClient;

import com.example.order_service.configuration.ProductClientFallbackFactory;
import com.example.order_service.dto.ProductGetVM;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "/search-service",
        configuration = FeignConfig.class,
        fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {
    @GetMapping(value = "/search-service/search/internal/get-product/{productId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ProductGetVM>> getProductDetails(
            @RequestHeader String token,
            @PathVariable String productId
    );
}
