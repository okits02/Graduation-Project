package com.example.profile_service.repository.httpClient;

import com.example.profile_service.dto.response.ApiResponse;
import com.example.profile_service.dto.response.GetUserIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserServiceClient {
    @GetMapping(value = "/users/getUserId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<GetUserIdResponse> getUserId(@RequestHeader("Authorization") String token);
}


