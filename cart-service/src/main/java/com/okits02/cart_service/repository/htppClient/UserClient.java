package com.okits02.cart_service.repository.htppClient;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.cart_service.dto.response.GetUserIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping(value = "/user-service/users/getUserId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<GetUserIdResponse> getUserId(@RequestHeader("Authorization") String token);
}

