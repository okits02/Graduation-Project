package com.example.profile_service.repository;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.entity.UserProfile;
import org.mapstruct.MappingTarget;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends Neo4jRepository<UserProfile, String> {
    UserProfile findByUserId(String userId);

    void deleteByUserId(String userId);
}
