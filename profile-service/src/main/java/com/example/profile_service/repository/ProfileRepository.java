package com.example.profile_service.repository;

import com.example.profile_service.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProfileRepository extends JpaRepository<UserProfile, String> {
    UserProfile findByUserId(String userId);
    Page<UserProfile> findAll(Pageable pageable);
    List<UserProfile> findByUserIdIn(List<String> userIds);
}
