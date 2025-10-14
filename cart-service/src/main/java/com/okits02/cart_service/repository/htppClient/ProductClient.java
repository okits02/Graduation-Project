package com.okits02.cart_service.repository.htppClient;


import com.okits02.cart_service.dto.ProductGetVM;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("search-service")
public interface ProductClient {
    @GetMapping(value = "/search-service/search/internal/get-product/{productId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ProductGetVM>> getProductDetails(
            @RequestHeader String token,
            @PathVariable String productId
    );
}