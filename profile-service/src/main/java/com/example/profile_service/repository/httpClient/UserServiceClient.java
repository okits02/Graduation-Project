package com.example.profile_service.repository.httpClient;

import com.example.profile_service.configuration.UserClientFallBackFactory;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.profile_service.dto.response.GetUserIdResponse;
import com.okits02.common_lib.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service",
        configuration = FeignConfig.class,
        fallbackFactory = UserClientFallBackFactory.class)
public interface UserServiceClient {
    @GetMapping(value = "/user-service/users/getUserId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<GetUserIdResponse> getUserId(@RequestHeader("Authorization") String token);
}


