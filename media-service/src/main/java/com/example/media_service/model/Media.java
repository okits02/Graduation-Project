package com.example.media_service.model;

import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String productId;
    String url;
    String publicId;
    @Enumerated(EnumType.STRING)
    MediaType mediaType;
    @Enumerated(EnumType.STRING)
    MediaPurpose mediaPurpose;
    @CreationTimestamp
    LocalDateTime createAt;
    @UpdateTimestamp
    LocalDateTime updateAt;
}
