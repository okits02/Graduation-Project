package com.example.product_service.repository;

import com.example.product_service.model.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeRepository extends JpaRepository<Attribute, String> {
    List<Attribute> findByCategoryId(String categoryId);
}
