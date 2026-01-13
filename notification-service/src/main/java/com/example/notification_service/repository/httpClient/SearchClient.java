package com.example.notification_service.repository.httpClient;

import com.example.notification_service.dto.ProductSkuVM;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "search-service")
public interface SearchClient {
    @GetMapping(value = "/search-service/search/internal/product/sku",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<ProductSkuVM>> getProductDetails(
            @RequestParam("skus") List<String> skus
    );
}
