package com.example.product_service.repository;

import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.model.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Products, String>{
    Page<Products> findAll(Pageable pageable);
}
