package com.example.userservice.repository;


import com.example.userservice.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);

    boolean existsByEmail(String email);
}
