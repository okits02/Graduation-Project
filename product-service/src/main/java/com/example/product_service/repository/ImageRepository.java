package com.example.product_service.repository;

import com.example.product_service.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ImageRepository extends JpaRepository<Image, String> {
    @Query(value = "SELECT * FROM image WHERE url_image = :url", nativeQuery = true)
    Image findByUrlImg(String url);
    @Query(value = "SELECT * FROM image WHERE id_product = :productId", nativeQuery = true)
    List<Image> findByProductsId(String productId);
}
