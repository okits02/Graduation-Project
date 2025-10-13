package com.okits02.common_lib.exception;

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
