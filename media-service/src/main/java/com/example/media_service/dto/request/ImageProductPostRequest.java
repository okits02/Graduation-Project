package com.example.media_service.dto.request;

import com.example.media_service.validator.ListFileImageConstraint;
import com.example.media_service.validator.ThumbnailFileValidConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageProductPostRequest {
    String productId;
    @ListFileImageConstraint(allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"},
            message = "File type not allowed. Allowed types are: JPEG, PNG, GIF")
    List<MultipartFile> imageProducts;
}
