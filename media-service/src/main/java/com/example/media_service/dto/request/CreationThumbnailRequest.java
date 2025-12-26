package com.example.media_service.dto.request;

import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.validator.ThumbnailFileValidConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreationThumbnailRequest {
    String productId;
    String ownerId;
    MediaOwnerType type;
    @ThumbnailFileValidConstraint(allowedTypes = {"image/jpeg", "image/png", "image/gif"},
            message = "File type not allowed. Allowed types are: JPEG, PNG, GIF")
    MultipartFile thumbnail;
}
