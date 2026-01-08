package com.example.media_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "profile-service")
public interface ProfileClient {
    @DeleteMapping(value = "/profile-service/profile/internal/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<?> creationAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "avatarUrl") String avatarUrl);
}
