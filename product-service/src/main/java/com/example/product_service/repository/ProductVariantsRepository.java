package com.example.product_service.repository;

import com.example.product_service.model.ProductVariants;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductVariantsRepository extends MongoRepository<ProductVariants, String> {
    @Query(value = "{'product_id': ?0, 'variant_name': ?1, 'best_specifications.color': ?2}", exists = true)
    boolean existsByProductIdAndNameAndColor(
            String productId,
            String name,
            String color
    );

    ProductVariants findBySku(String sku);

    List<ProductVariants> findByProductId(String productId);
}
