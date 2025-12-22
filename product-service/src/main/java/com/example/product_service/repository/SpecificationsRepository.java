package com.example.product_service.repository;

import com.example.product_service.model.Specifications;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpecificationsRepository extends MongoRepository<Specifications, String> {
}
