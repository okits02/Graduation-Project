package com.example.product_service.repository;


import com.example.product_service.model.Products;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Products, String> {
    Page<Products> findAll(Pageable pageable);
}
