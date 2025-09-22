package com.example.media_service.dto.request;

import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.validator.ThumbnailFileValidConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageUploadRequest {
    String ownerId;
    @ThumbnailFileValidConstraint(allowedTypes = {"image/jpeg", "image/png", "image/gif"},
            message = "File type not allowed. Allowed types are: JPEG, PNG, GIF")
    MultipartFile multipartFile;
}
