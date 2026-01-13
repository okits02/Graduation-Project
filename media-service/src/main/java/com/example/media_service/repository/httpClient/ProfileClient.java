package com.example.media_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "profile-service")
public interface ProfileClient {
    @PostMapping(value = "/profile-service/profile/internal/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> creationAvatar(
            @RequestParam(value = "avatarUrl") String avatarUrl,
            @RequestParam(value = "userId") String userId
    );
}
