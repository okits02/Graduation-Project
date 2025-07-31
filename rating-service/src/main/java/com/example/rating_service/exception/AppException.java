package com.example.rating_service.exception;

import com.example.rating_service.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
  public AppException(ErrorCode errorCode) {
    super(errorCode.getMessage());
      this.errorCode = errorCode;
  }

  private final ErrorCode errorCode;
}
