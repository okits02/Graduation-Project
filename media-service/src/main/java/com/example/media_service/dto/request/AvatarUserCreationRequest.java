package com.example.media_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvatarUserCreationRequest {
    MultipartFile avatarFile;
}
