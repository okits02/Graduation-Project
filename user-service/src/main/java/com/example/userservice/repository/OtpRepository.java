package com.example.userservice.repository;

import com.example.userservice.model.OTP;
import com.example.userservice.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OTP, String> {
    List<OTP> user(Users user);
    Optional<OTP> findByUserId(String userId);
}
