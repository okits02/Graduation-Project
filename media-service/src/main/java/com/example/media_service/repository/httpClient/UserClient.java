package com.example.media_service.repository.httpClient;

import com.example.media_service.dto.response.GetUserIdResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping(value = "/user-service/users/getUserId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<GetUserIdResponse> getUserId(@RequestHeader("Authorization") String token);
}
