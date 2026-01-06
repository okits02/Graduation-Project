package com.example.order_service.repository.httpClient;

import com.example.order_service.dto.GetListSkuVM;
import com.example.order_service.dto.ProductSkuVM;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "search-service")
public interface ProductClient {
    @GetMapping(value = "/search-service/search/internal/product/sku",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<ProductSkuVM>> getProductDetails(
            @RequestParam("skus") List<String> skus
    );
    @GetMapping(value = "/search-service/search/internal/product/list-sku",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<GetListSkuVM> getListSkuByProductById(
            @RequestParam("productId") String productId
    );
}
