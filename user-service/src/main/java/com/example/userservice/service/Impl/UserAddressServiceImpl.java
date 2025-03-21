package com.example.userservice.service.Impl;

import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.request.UserAddressUpdateRequest;
import com.example.userservice.dto.response.UserAddressResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.mapper.UserAddressMapper;
import com.example.userservice.model.UserAddress;
import com.example.userservice.model.Users;
import com.example.userservice.repository.UserAddressRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserAddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserAddressMapper userAddressMapper;

    @Override
    public void addUserAddressToUser(String userId, UserAddressCreateRequest userAddress) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        userAddress.setUser(users);
        userAddressRepository.save(userAddressMapper.toUserAddress(userAddress));
    }

    @Override
    public void deleteAddressFromUser(String userId, String addressId) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        UserAddress userAddress = userAddressRepository.findById(addressId).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTS));
        if(userAddress.getUser().equals(users)){
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }
        users.getAddressList().remove(userAddress);
        userAddressRepository.delete(userAddress);
    }

    @Override
    public void deleteMyAddress(String addressId) {
        var context = SecurityContextHolder.getContext();
        String currentName = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentName).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        UserAddress userAddress = userAddressRepository.findById(addressId).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTS));
        if(userAddress.getUser() == null || !userAddress.getUser().getUsername().equals(users.getUsername())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }
        users.getAddressList().remove(userAddress);
        userAddressRepository.delete(userAddress);
    }

    @Override
    public void updateMyAddress(UserAddressUpdateRequest request) {
        var context = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = userRepository.findByUsername(context).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        UserAddress userAddress = userAddressRepository.findById(request.getId()).orElseThrow(()-> new AppException(ErrorCode.ADDRESS_NOT_EXISTS));
        if(!userAddress.getUser().equals(users)){
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }
        userAddressMapper.updateUserAddressFormRequest(request,userAddress);
        userAddressRepository.save(userAddress);
    }

    @Override
    public Page<UserAddressResponse> getAllUserAddresses(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        if(users.getAddressList().isEmpty())
        {
            throw new AppException(ErrorCode.ADDRESS_NOT_EXISTS);
        }
        return userAddressRepository.findByUserId(userId, pageable);
    }
}
