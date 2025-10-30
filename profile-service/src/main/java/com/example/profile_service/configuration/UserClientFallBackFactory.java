package com.example.profile_service.configuration;

import com.example.profile_service.dto.response.GetUserIdResponse;
import com.example.profile_service.repository.httpClient.UserServiceClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.okits02.common_lib.feign.BaseFallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallBackFactory extends BaseFallbackFactory<UserServiceClient> {
    @Override
    protected UserServiceClient createFallbackInstance(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public ApiResponse<GetUserIdResponse> getUserId(String token) {
                throw new AppException(GlobalErrorCode.INTERNAL_ERROR);
            }
        };
    }

    @Override
    protected String getClientName() {
        return "user-service";
    }

}
