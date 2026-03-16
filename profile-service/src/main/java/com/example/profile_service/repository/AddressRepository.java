package com.example.profile_service.repository;

import com.example.profile_service.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AddressRepository extends JpaRepository<UserAddress, String> {
}
