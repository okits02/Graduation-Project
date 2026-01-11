package com.example.search_service.repository.httpClient;

import com.example.search_service.model.Promotion;
import com.example.search_service.viewmodel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "promotion-service")
public interface PromotionClient {
    @GetMapping(value = "/promotion-service/promotion/internal/get-promotion-by-cate",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<Promotion>> getByCategoryIds(@RequestParam List<String> categoryIds);
}
