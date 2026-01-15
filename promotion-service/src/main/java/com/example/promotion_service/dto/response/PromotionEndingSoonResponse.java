package com.example.promotion_service.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionEndingSoonResponse {
    LocalDateTime expiredAt;
    List<String> promotionId;
}
