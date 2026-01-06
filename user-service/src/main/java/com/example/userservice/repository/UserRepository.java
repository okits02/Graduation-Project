package com.example.userservice.repository;


import com.example.userservice.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);
    @Query(value = """
    SELECT DISTINCT u
    FROM Users u
    JOIN u.roles r
    WHERE r.name = 'USER'
    """, nativeQuery = true)
    Page<Users> getAll(Pageable pageable);
}
