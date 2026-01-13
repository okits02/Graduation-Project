package com.example.media_service.dto.response;

import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BannerResponse {
    String id;
    String ownerId;
    String bannerUrl;
    MediaPurpose mediaPurpose;
}
