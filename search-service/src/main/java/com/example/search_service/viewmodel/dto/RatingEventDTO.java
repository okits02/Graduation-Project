package com.example.search_service.viewmodel.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingEventDTO {
    String productId;
    Double avgRating;
}
