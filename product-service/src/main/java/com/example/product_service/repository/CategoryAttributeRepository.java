package com.example.product_service.repository;

import com.example.product_service.model.CategoryAttribute;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, String> {
}
