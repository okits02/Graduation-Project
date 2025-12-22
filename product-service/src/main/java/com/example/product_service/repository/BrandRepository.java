package com.example.product_service.repository;

import com.example.product_service.model.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrandRepository extends MongoRepository<Brand, String> {
    boolean existsByName(String name);

    Brand findByName(String name);
}
