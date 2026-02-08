package com.example.userservice.repository;


import com.example.userservice.model.Users;
import feign.Param;
import org.apache.catalina.User;
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

    @Query("""
    SELECT u
    FROM Users u
    JOIN u.role r
    WHERE r.name = 'ADMIN'
    """)
    Users findByRole();

    @Query("""
    SELECT u.username
    FROM Users u
    WHERE LOWER(u.username) LIKE LOWER(CONCAT(:keyword, '%'))
    """)
    List<String> findUsernameAutocomplete(@Param("keyword")String keyword);

    @Query("""
    SELECT u.email
    FROM Users u
    WHERE LOWER(u.email) LIKE LOWER(CONCAT(:keyword, '%')) 
    """)
    List<String> findEmailAutocomplete(@Param("keyword") String keyword);

    @Query(value = """
    SELECT u.username
    FROM Users u
    WHERE MATCH(username)
    AGAINST (:username IN NATURAL LANGUAGE MODE)
    """, countQuery = """
    SELECT COUNT(*)
    FROM Users u
    WHERE MATCH(username)
    AGAINST (:username IN NATURAL LANGUAGE MODE)
    """, nativeQuery = true)
    Page<Users> searchByUserName(@Param("username") String userName, Pageable pageable);

    @Query(value = """
    SELECT u.email
    WHERE Users u
    AGAINST (:email IN NATURAL LANGUAGE MODE)
    """, countQuery = """
    SELECT COUNT(*)
    FROM Users u
    AGAINST (:username IN NATURAL LANGUAGE MODE)
    """, nativeQuery = true)
    Page<Users> searchByEmail(@Param("email") String email, Pageable pageable);
}
