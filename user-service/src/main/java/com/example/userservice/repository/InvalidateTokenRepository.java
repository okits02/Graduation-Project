package com.example.userservice.repository;

import com.example.userservice.model.InvalidateToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken, String> {
}
