package com.example.profile_service.repository;

import com.example.profile_service.entity.UserAddress;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends Neo4jRepository<UserAddress, String> {
}
