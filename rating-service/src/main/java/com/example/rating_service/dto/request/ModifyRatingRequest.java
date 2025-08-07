package com.example.rating_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModifyRatingRequest {
    String content;
    String ratingScore;
    String userName;
}
