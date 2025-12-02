package com.okits02.payment_service.repository.httpClient;

import com.okits02.payment_service.dto.response.UserIdResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "user-service")
public interface UserClient {
    @GetMapping(value = "/users/getUserId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserIdResponse> getUserId(@RequestHeader("Authorization") String token);
}
