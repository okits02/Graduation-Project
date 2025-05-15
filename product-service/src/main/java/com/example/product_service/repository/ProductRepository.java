package com.example.product_service.repository;

import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.model.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;

public interface ProductRepository extends JpaRepository<Products, String>{
    Page<ProductResponse> findAll(Pageable pageable);
}
