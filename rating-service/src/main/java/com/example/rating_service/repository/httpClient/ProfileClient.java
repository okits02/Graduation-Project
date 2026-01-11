package com.example.rating_service.repository.httpClient;

import com.example.rating_service.dto.CustomerVM;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "profile-service")
public interface ProfileClient {
    @GetMapping(value = "/profile-service/profile/internal/rating/getProfile",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CustomerVM>> getProfileForRating(@RequestParam("userId") String userId);
}
