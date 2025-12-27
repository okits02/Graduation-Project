package com.example.media_service.kafka;

import jakarta.annotation.security.DenyAll;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyThumbnailEvent {
    String productId;
    String ownerId;
    String mediaOwnerType;
    String url;
}
