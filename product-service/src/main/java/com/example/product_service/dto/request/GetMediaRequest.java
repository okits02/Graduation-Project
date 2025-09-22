package com.example.product_service.dto.request;

import com.example.product_service.enums.MediaOwnerType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetMediaRequest {
    String ownerId;
    MediaOwnerType mediaOwnerType;
}
