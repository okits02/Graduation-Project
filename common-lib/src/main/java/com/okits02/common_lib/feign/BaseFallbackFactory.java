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

        return createFallbackInstance(cause);
    }

    protected abstract T createFallbackInstance(Throwable cause);

    protected abstract String getClientName();
}
