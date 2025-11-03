package com.example.order_service.configuration;

import com.example.order_service.dto.response.UserIdResponse;
import com.example.order_service.repository.httpClient.UserClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory extends BaseFallbackFactory<UserClient> {
    @Override
    protected UserClient createFallbackInstance(Throwable cause) {
        return new UserClient() {
            @Override
            public ResponseEntity<ApiResponse<UserIdResponse>> getUserId(String token) {
                log.warn("Fallback: cannot delete profile for user {} because {}", token, cause.getMessage());
                throw new AppException(GlobalErrorCode.SERVICE_UNAVAILABLE);
            }
        };
    }

    @Override
    protected String getClientName() {
        return "user-service";
    }

}
