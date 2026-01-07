package com.example.rating_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentUpdateRequest {
    String id;
    String content;
    String productId;
    String userId;
}
