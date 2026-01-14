package com.example.rating_service.dto.response;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String firstName;
    String lastName;
    String avatarUrl;
    String userId;
    String content;
    String productId;
    String parentId;
    List<CommentResponse> childrent;
    LocalDateTime createdAt;
}
