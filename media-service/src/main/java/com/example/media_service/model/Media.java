package com.example.media_service.model;

import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String ownerId;
    @Enumerated(EnumType.STRING)
    private MediaOwnerType ownerType;
    String url;
    String publicId;
    @Enumerated(EnumType.STRING)
    MediaType mediaType;
    @Enumerated(EnumType.STRING)
    MediaPurpose mediaPurpose;
    Integer position;
    @CreationTimestamp
    LocalDateTime createAt;
    @UpdateTimestamp
    LocalDateTime updateAt;
}
