package com.example.order_service.repository.httpClient;

import com.example.order_service.dto.response.ProfileResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.*;
import java.util.List;

@FeignClient(name = "profile-service")
public interface ProfileClient {
    @GetMapping(value = "/profile-service/profile/internal/order/getProfile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<java.util.List<ProfileResponse>> getProfileForOrder(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "userIds") List<String> userIds
    );
}
