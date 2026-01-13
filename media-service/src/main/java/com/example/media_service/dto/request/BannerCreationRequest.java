package com.example.media_service.dto.request;

import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.validator.ListFileImageConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BannerCreationRequest {
    String ownerId;
    String ownerType;
    MediaPurpose mediaPurpose;
    @ListFileImageConstraint(allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"},
            message = "File type not allowed. Allowed types are: JPEG, PNG, GIF")
    MultipartFile imageBanner;
}
