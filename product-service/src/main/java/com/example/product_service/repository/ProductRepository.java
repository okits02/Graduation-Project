package com.example.product_service.repository;


import com.example.product_service.model.Products;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;

public interface ProductRepository extends MongoRepository<Products, String> {
    Page<Products> findAll(Pageable pageable);

    @Query(value = "{'name' : ?0}", exists = true)
    boolean existsByName(String name);

    @Query(value = "{'categoryId' : ?0}")
    List<Products> findByCategoryId(String categoryId);

    @Modifying
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'inStock': ?1 } }")
    void updateStockById(String productId, boolean inStock);

}
