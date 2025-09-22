package com.example.media_service.dto.request;

import com.example.media_service.enums.MediaOwnerType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteMediaRequest {
    String ownerId;
    MediaOwnerType mediaOwnerType;
}
