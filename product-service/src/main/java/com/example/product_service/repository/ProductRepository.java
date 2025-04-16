package com.example.product_service.repository;

import com.example.product_service.model.Products;
import com.example.product_service.repository.ProductRepositoryCustom.ProductRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Products, String>, ProductRepositoryCustom {
}
