package com.example.product_service.dto.response;

import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.enums.MediaPurpose;
import com.example.product_service.enums.MediaType;
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
}
