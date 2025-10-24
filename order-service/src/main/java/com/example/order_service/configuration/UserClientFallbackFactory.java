package com.example.order_service.configuration;

import com.example.order_service.dto.ProductGetVM;
import com.example.order_service.dto.response.UserIdResponse;
import com.example.order_service.repository.httpClient.ProductClient;
import com.example.order_service.repository.httpClient.UserClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.okits02.common_lib.feign.BaseFallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory extends BaseFallbackFactory<UserClient> {
    @Override
    protected String getClientName() {
        return "user-service";
    }

    @Override
    public UserClient create(Throwable cause){
        super.create(cause);
        return new UserClient() {

            @Override
            public ResponseEntity<ApiResponse<UserIdResponse>> getUserId(String token) {
                throw new AppException(GlobalErrorCode.INTERNAL_ERROR);
            }
        };
    }
}
