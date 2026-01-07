package com.example.rating_service.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingSummaryResponse {
    Double averageRating;
    Integer totalReviews;
    Integer maxRating;
}
