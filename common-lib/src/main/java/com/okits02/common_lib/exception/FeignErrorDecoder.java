package com.okits02.common_lib.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okits02.common_lib.dto.DownstreamResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String s, Response response) {
        String errorMessage = "Downstream service errors";
        int status = response.status();
        try {
            if(response.body() != null){
                InputStream inputStream = response.body().asInputStream();
                DownstreamResponse downstreamResponse = objectMapper.readValue(inputStream, DownstreamResponse.class);
                errorMessage = downstreamResponse.getMessage();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        switch (response.status()){
            case 400:
                return new AppException(GlobalErrorCode.FEIGN_BAD_REQUEST, errorMessage);
            case 401:
                return new AppException(GlobalErrorCode.FEIGN_UNAUTHORIZED, errorMessage);
            case 404:
                return new AppException(GlobalErrorCode.FEIGN_NOT_FOUND, errorMessage);
            case 500:
                return new AppException(GlobalErrorCode.FEIGN_INTERNAL_ERROR, errorMessage);
            default:
                return new AppException(GlobalErrorCode.FEIGN_INTERNAL_ERROR, errorMessage);
        }
    }
}
