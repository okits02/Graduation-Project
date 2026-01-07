package com.example.profile_service.repository;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.entity.UserProfile;
import feign.Param;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProfileRepository extends Neo4jRepository<UserProfile, String> {
    UserProfile findByUserId(String userId);
    Page<UserProfile> findAll(Pageable pageable);
    void deleteByUserId(String userId);
    @Query("""
        MATCH (p:user_profile)
        WHERE p.userId IN $userIds
        RETURN p
    """)
    List<UserProfile> findByUserIds(@Param("userIds") List<String> userIds);
}
