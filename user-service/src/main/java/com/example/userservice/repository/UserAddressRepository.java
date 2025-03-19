package com.example.userservice.repository;

import com.example.userservice.dto.response.UserAddressResponse;
import com.example.userservice.model.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, String> {
    List<UserAddress> findByUserId(String userId);
    Page<UserAddressResponse> findByUserId(String userId, Pageable pageable);
}
