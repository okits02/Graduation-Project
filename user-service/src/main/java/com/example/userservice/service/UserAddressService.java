package com.example.userservice.service;


import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.response.UserAddressResponse;

import java.util.List;

public interface UserAddressService {
    void addUserAddressToUser(String userId, UserAddressCreateRequest userAddress);
    void deleteAddressFromUser(String userId, UserAddressCreateRequest userAddress);
    void deleteAddress(String addressId);
    void updateUserAddress(String userId, UserAddressCreateRequest userAddress);
    List<UserAddressResponse> getAllUserAddresses(String userId);
}
