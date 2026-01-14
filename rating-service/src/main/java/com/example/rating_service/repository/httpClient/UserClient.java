package com.example.rating_service.repository.httpClient;

import com.example.rating_service.dto.response.UserIdResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.print.attribute.standard.Media;

@FeignClient(value = "user-service")
public interface UserClient {
    @GetMapping(value = "/user-service/users/getUserId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserIdResponse> getUserId(@RequestHeader("Authorization") String token);

}
