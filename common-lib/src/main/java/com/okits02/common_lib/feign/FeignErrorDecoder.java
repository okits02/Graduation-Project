package com.okits02.common_lib.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign call failed : {}, statsus : {}", methodKey, response);
        ApiResponse<?> apiResponse;
        try(InputStream body = response.body().asInputStream()){
            apiResponse = objectMapper.convertValue(body, ApiResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new AppException(new ErrorCode() {
            @Override
            public int getCode() {
                return apiResponse.getCode();
            }

            @Override
            public String getMessage() {
                return apiResponse.getMessage();
            }

            @Override
            public HttpStatusCode getHttpStatusCode() {
                return HttpStatusCode.valueOf(response.status());
            }
        });
    }
}
