package com.okits02.inventory_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.inventory_service.dto.response.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping(value = "/product-service/product/internal/variant", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ProductVariantResponse> getVariantBySku(@RequestParam(value = "sku") String sku);
}
