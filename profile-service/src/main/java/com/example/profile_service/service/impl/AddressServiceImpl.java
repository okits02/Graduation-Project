package com.example.profile_service.service.impl;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.response.AddressResponse;
import com.example.profile_service.mapper.AddressMapper;
import com.example.profile_service.repository.AddressRepository;
import com.example.profile_service.service.AddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressServiceImpl implements AddressService {
    AddressRepository addressRepository;
    AddressMapper addressMapper;

    @Override
    public AddressResponse createAddress(AddressRequest request) {
        return null;
    }

    @Override
    public AddressResponse getMyAddress(String userId) {
        return null;
    }

    @Override
    public AddressResponse updateMyAddress(AddressRequest request) {
        return null;
    }

    @Override
    public void deleteAddress(String addressId) {

    }
}
