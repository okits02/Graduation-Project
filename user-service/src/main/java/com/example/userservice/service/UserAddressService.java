package com.example.userservice.service;


import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.request.UserAddressUpdateRequest;
import com.example.userservice.dto.response.UserAddressResponse;
import org.springframework.data.domain.Page;


public interface UserAddressService {
    void addUserAddressToUser(String userId, UserAddressCreateRequest userAddress);
    void deleteAddressFromUser(String userId, String addressId);
    void deleteMyAddress(String addressId);
    void updateMyAddress(UserAddressUpdateRequest userAddress);
    Page<UserAddressResponse> getAllUserAddresses(String userId, int page, int size);
}
