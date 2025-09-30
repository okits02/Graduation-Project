package com.example.media_service.repository;

import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, String> {
    List<Media> findByOwnerIdAndOwnerType(String ownerId,
                                          MediaOwnerType ownerType);

    Media findByUrl(String url);

    boolean existsByOwnerIdAndMediaPurpose(String ownerId, MediaPurpose mediaPurpose);
}
