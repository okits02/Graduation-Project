package com.example.userservice.configuration;

import com.example.userservice.repository.httpClient.ProfileClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.okits02.common_lib.feign.BaseFallbackFactory;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
public class ProfileClientFallBackFactory extends BaseFallbackFactory<ProfileClient> {

    @Override
    protected ProfileClient createFallbackInstance(Throwable cause) {
        return new ProfileClient() {
            @Override
            public ResponseEntity<ApiResponse<Void>> deleteMyProfile(String token, String userId) {
                log.warn("Fallback: cannot delete profile for user {} due to {}", userId, cause.toString());

                if (cause instanceof RetryableException || cause.getCause() instanceof SocketTimeoutException) {
                    throw new AppException(GlobalErrorCode.SERVICE_TIMEOUT);
                }

                if (cause.getCause() instanceof ConnectException) {
                    throw new AppException(GlobalErrorCode.SERVICE_UNAVAILABLE);
                }

                throw new AppException(GlobalErrorCode.INTERNAL_ERROR);
            }
        };
    }

    @Override
    protected String getClientName() {
        return "profile-service";
    }


}
