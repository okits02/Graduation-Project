package com.example.promotion_service.exception;

import com.example.promotion_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException runtimeException)
    {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(1001);
        apiResponse.setMessage(runtimeException.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlintAppException(AppException exception)
    {
        PromotionErrorCode promotionErrorCode = exception.getPromotionErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(promotionErrorCode.getCode());
        apiResponse.setMessage(promotionErrorCode.getMessage());
        return ResponseEntity.status(promotionErrorCode.getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException e){
        PromotionErrorCode promotionErrorCode = PromotionErrorCode.UNAUTHENTICATED;

        return ResponseEntity.status(promotionErrorCode.getHttpStatus()).body(
                ApiResponse.builder()
                        .code(promotionErrorCode.getCode())
                        .message(promotionErrorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<String> handlingValidator(MethodArgumentNotValidException e)
    {
        return ResponseEntity.badRequest().body(Objects.requireNonNull(e.getFieldError()).getDefaultMessage());
    }
}
