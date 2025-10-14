package com.example.order_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import com.example.order_service.dto.response.UserIdResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("/user-service")
public interface UserClient {
    @GetMapping(value = "/user-service/users/getUserId",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<UserIdResponse>> getUserId(
            @RequestHeader String token
    );
}
