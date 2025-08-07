package com.example.rating_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingRequest {
    String content;
    Double ratingScore;
    String productId;
    String productName;
    String createBy;
}
