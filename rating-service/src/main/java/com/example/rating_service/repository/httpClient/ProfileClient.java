package com.example.rating_service.repository.httpClient;

import com.example.rating_service.dto.CustomerVM;
import com.example.rating_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-service")
public interface ProfileClient {
    @GetMapping(value = "/profile-service/profile/rating/getProfile", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CustomerVM>> getProfileForRating(@RequestHeader String token);
}
