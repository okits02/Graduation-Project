package com.example.media_service.dto.response;

import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaResponse {
    String id;
    String ownerId;
    MediaOwnerType ownerType;
    String url;
    MediaPurpose mediaPurpose;
    MediaType mediaType;
    Integer position;
}
