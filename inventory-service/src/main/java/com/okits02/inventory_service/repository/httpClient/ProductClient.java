package com.okits02.inventory_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.inventory_service.dto.response.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "search-service")
public interface ProductClient {
    @GetMapping(value = "/search-service/search/internal/product/sku", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<List<ProductVariantResponse>> getVariantBySku(
            @RequestParam(value = "skus") List<String> skus);
}
