package com.example.media_service.repository;

import com.example.media_service.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, String> {
    List<Media> findByProductId(String productId);

    Media findByUrl(String url);
}
