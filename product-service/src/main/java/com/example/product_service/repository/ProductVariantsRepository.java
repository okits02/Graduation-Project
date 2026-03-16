package com.example.product_service.repository;

import com.example.product_service.dto.request.SpecificationRequest;
import com.example.product_service.model.ProductVariants;
import com.example.product_service.model.Specifications;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductVariantsRepository extends MongoRepository<ProductVariants, String> {
    @Query(value = "{'product_id': ?0, 'variant_name': ?1, 'best_specifications.key': ?2, 'best_specifications.value': ?3}"
            , exists = true)
    boolean existsByProductIdAndNameAndSpec(
            String productId,
            String name,
            String key,
            String value
    );

    boolean existsByProductId(String productId);

    ProductVariants findBySku(String sku);

    List<ProductVariants> findByProductId(String productId);
}
