package com.example.search_service.viewmodel.dto.response;

import com.example.search_service.enums.MediaOwnerType;
import com.example.search_service.enums.MediaPurpose;
import com.example.search_service.enums.MediaType;
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
