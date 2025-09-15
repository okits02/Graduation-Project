package com.example.product_service.repository.httpsClient;

import com.example.product_service.dto.RemoveCategoryRequest;
import com.example.product_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "search-service")
public interface SearchClient {
    @PutMapping(value = "/search-service/search/internal/categories/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Long>> removeCate(
            @RequestBody RemoveCategoryRequest request
            );
}