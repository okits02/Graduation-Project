package com.example.userservice.repository;


import com.example.userservice.model.Users;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);
    @Query(value = """
    SELECT DISTINCT u
    FROM Users u
    JOIN u.role r
    WHERE r.name = 'USER'
    """)
    Page<Users> getAll(Pageable pageable);

    @Query("""
        SELECT DISTINCT u.email
        FROM Users u
        WHERE u.id IN :userIds
          AND u.email IS NOT NULL
    """)
    List<String> findEmailsByUserIds(
            @Param("userIds") List<String> userIds
    );
}
