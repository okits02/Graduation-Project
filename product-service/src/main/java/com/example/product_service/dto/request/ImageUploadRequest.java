package com.example.product_service.dto.request;

import com.example.product_service.enums.MediaOwnerType;
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
    String productId;
    String ownerId;
    MediaOwnerType mediaOwnerType;
    MultipartFile multipartFile;
}
