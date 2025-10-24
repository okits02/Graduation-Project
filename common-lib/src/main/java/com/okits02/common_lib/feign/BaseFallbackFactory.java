package com.okits02.common_lib.feign;

import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
public abstract class BaseFallbackFactory<T> implements FallbackFactory<T> {

    @Override
    public T create(Throwable cause) {
        log.error("{} fallback triggered: {}", getClientName(), cause.getMessage(), cause);
        if(cause.getCause() instanceof AppException appEx){
            log.warn("AppException from FeignErrorDecoder: code={}, message={}",
                    appEx.getErrorCode().getCode(), appEx.getErrorCode().getMessage());
            throw appEx;
        }

        if(cause instanceof RetryableException || cause.getCause() instanceof SocketTimeoutException){
            log.warn("{} timeout/network issue", getClientName());
            throw new AppException(GlobalErrorCode.SERVICE_TIMEOUT);
        }

        if(cause.getCause() instanceof ConnectException ) {
            log.warn("{} connection refused!", getClientName());
            throw new AppException(GlobalErrorCode.SERVICE_UNAVAILABLE);
        }

        log.error("Unknown error in {} fallback", getClientName());
        throw new AppException(GlobalErrorCode.INTERNAL_ERROR);
    }

    protected abstract String getClientName();
}
