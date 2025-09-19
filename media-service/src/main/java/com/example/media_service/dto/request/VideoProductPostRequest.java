package com.example.media_service.dto.request;

import com.example.media_service.model.enums.MediaTypes;
import com.example.media_service.validator.VideoValidConstraint;
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
public class VideoProductPostRequest {
    String productId;
    @VideoValidConstraint(allowedTypes = {"video/mp4", "video/webm", "video/quicktime", "video/x-matroska"},
            message = "File type not allowed. Allowed types are: JPEG, PNG, GIF")
    MultipartFile videoProduct;
    MediaTypes mediaTypes;
}
