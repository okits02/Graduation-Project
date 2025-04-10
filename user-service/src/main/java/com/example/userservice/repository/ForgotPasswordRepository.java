package com.example.userservice.repository;

import com.example.userservice.model.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, String> {
    Optional<ForgotPassword> findByUserId(String userId);

    void deleteById(String id);
}
