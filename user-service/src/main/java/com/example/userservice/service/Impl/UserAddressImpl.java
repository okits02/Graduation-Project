package com.example.userservice.service.Impl;

import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.response.UserAddressResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.mapper.UserAddressMapper;
import com.example.userservice.model.Users;
import com.example.userservice.repository.UserAddressRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserAddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAddressImpl implements UserAddressService {
    UserRepository userRepository;
    UserAddressRepository userAddressRepository;
    UserAddressMapper userAddressMapper;
    @Override
    public void addUserAddressToUser(String userId, UserAddressCreateRequest userAddress) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        userAddress.setUser(users);
        userAddressRepository.save(userAddressMapper.toUserAddress(userAddress));
    }

    @Override
    public void deleteAddressFromUser(String userId, UserAddressCreateRequest userAddress) {

    }

    @Override
    public void deleteAddress(String addressId) {

    }

    @Override
    public void updateUserAddress(String userId, UserAddressCreateRequest userAddress) {

    }

    @Override
    public List<UserAddressResponse> getAllUserAddresses(String userId) {
        return List.of();
    }
}
