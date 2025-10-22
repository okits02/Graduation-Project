package com.example.userservice.repository.httpClient;

import com.example.userservice.configuration.ProfileClientFallBackFactory;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-service",
        configuration = FeignConfig.class,
        fallbackFactory = ProfileClientFallBackFactory.class)
public interface ProfileClient {
    @DeleteMapping(value = "/profile-service/profile/admin/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Void>> deleteMyProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId);
}
