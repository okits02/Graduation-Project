package com.example.userservice.repository;

import com.example.userservice.model.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, String> {
    ForgotPassword findByUserId(String userId);

    void delete(String id);
}
