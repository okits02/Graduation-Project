package com.example.promotion_service.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
  public AppException(PromotionErrorCode promotionErrorCode) {
    super(promotionErrorCode.getMessage());
      this.promotionErrorCode = promotionErrorCode;
  }

  private final PromotionErrorCode promotionErrorCode;
}
