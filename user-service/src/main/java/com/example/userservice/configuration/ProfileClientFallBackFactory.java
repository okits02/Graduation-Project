package com.example.userservice.configuration;

import com.example.userservice.exception.UserErrorCode;
import com.example.userservice.repository.httpClient.ProfileClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.okits02.common_lib.feign.BaseFallbackFactory;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
public class ProfileClientFallBackFactory extends BaseFallbackFactory<ProfileClient> {

    @Override
    protected String getClientName() {
        return "profile-service";
    }

    @Override
    public ProfileClient create(Throwable cause) {
        super.create(cause);

        return new ProfileClient() {
            @Override
            public ResponseEntity<ApiResponse<Void>> deleteMyProfile(String token, String userId) {
                log.warn("Fallback: cannot delete profile for user {}", userId);
                throw new AppException(UserErrorCode.CAN_NOT_CONNECT_TO_PROFILE);
            }
        };
    }
}
