package com.example.userservice.repository.httpClient;

import com.example.userservice.configuration.FeignSupportConfig;
import com.example.userservice.dto.response.ProfileResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "profile-service",
        configuration = FeignSupportConfig.class)
public interface ProfileClient {
    @DeleteMapping(value = "/profile-service/profile/admin/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> deleteMyProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId);

    @GetMapping(value = "/profile-service/profile/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProfileResponse> getUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "userId") String userId);

    @GetMapping(value = "/profile-service/profile/myInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProfileResponse> getMyInFo(@RequestHeader("Authorization") String token);
    @GetMapping(value = "/profile-service/profile/internal/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<ProfileResponse>> getListUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "userIds") List<String> userIds);
}
