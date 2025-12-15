package com.example.promotion_service.repository.httpClient;

import com.example.promotion_service.dto.request.CategoryLevelValidateRequest;
import com.example.promotion_service.dto.response.ApiResponse;
import com.example.promotion_service.dto.response.CategoryLevelValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "product-service")
public interface ProductClient {
    @PostMapping(value = "/product-service/category/internal/validate-same-level",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<CategoryLevelValidateResponse> CategoryValidateSameLevel(
            @RequestBody CategoryLevelValidateRequest request
            );
}
