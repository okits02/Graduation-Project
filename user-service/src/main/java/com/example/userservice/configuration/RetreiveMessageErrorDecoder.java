package com.example.userservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.io.IOException;
import java.io.InputStream;

public class RetreiveMessageErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();
    @Override
    public Exception decode(String s, Response response) {
        ApiResponse apiResponse = null;
        try(InputStream bodyIs = response.body().asInputStream()){
            ObjectMapper objectMapper = new ObjectMapper();
            apiResponse = objectMapper.readValue(bodyIs, ApiResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        switch (response.status()) {
            case 400:
                return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        apiResponse.getMessage() != null ? apiResponse.getMessage() : "Bad Request");
            case 404:
                return new ResponseStatusException(HttpStatus.NOT_FOUND,
                        apiResponse.getMessage() != null ? apiResponse.getMessage() : "Not Found");
            default:
                return errorDecoder.decode(s, response);
        }
    }
}
