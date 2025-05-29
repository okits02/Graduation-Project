package com.example.product_service.repository;

import com.example.product_service.model.Image;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface ImageRepository extends MongoRepository<Image, String> {
    Image findByUrlImg(String url);
    List<Image> findByProductId(String productId);
}
