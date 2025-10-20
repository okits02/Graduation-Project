package com.okits02.common_lib.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.ErrorCode;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();
    ApiResponse<?> apiResponse = new ApiResponse<>();
    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign call failed : {}, status : {}", methodKey, response.status());

        try (InputStream body = response.body().asInputStream()) {
            apiResponse = objectMapper.readValue(body, ApiResponse.class);
        } catch (IOException e) {
            log.error("Error reading Feign response body", e);
        }

        // Tạo AppException
        AppException appException = new AppException(new ErrorCode() {
            @Override
            public int getCode() {
                return apiResponse != null ? apiResponse.getCode() : response.status();
            }

            @Override
            public String getMessage() {
                return apiResponse != null ? apiResponse.getMessage() : "Unknown error";
            }

            @Override
            public HttpStatusCode getHttpStatusCode() {
                return HttpStatusCode.valueOf(response.status());
            }
        });

        // Wrap vào FeignException để fallback nhận được
        return (Exception) FeignException.errorStatus(methodKey, response)
                .initCause(appException);
    }
}
