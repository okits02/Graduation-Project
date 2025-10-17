package com.example.userservice.configuration;

import com.example.userservice.repository.httpClient.ProfileClient;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import feign.FeignException;
import feign.RetryableException;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
public class ProfileClientFallBackFactory implements FallbackFactory<ProfileClient> {
    @Override
    public ProfileClient create(Throwable cause) {
        log.error("profile client fallback triggered: {}", cause.getMessage(), cause);
        return (token, userId) -> {
            if(cause instanceof RetryableException || cause.getCause() instanceof SocketTimeoutException){
                log.warn("TimeOut when calling profile-service: {}", cause.getMessage());
                throw new AppException(GlobalErrorCode.UNAUTHORIZED);
            }

            if(cause.getCause() instanceof ConnectException){
                log.warn("Connect refused: profile-service not reachable");
                throw new AppException(GlobalErrorCode.UNAUTHORIZED);
            }

            if(cause instanceof DecodeException){
                log.warn("Failed to decode Feign response from profile-service", cause);
                throw new AppException(GlobalErrorCode.UNAUTHORIZED);
            }

            if (cause instanceof FeignException feignEx) {
                int status = feignEx.status();
                log.error("Feign HTTP error from profile-service: {}", status);

                if (status >= 500) {
                    // lỗi server
                    throw new AppException(GlobalErrorCode.UNAUTHORIZED);
                } else if (status == 404) {
                    throw new AppException(GlobalErrorCode.UNAUTHORIZED);
                } else {
                    throw new AppException(GlobalErrorCode.UNAUTHORIZED);
                }
            }

            log.error("Unknown error when calling profile-service: {}", cause.getMessage());
            throw new AppException(GlobalErrorCode.UNAUTHORIZED);
        };
    }
}
