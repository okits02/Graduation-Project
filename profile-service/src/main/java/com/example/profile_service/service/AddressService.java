package com.example.profile_service.service;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    public AddressResponse createAddress(AddressRequest request);
    public List<AddressResponse> getAllMyAddress(String userId);
    public AddressResponse updateMyAddress(String userId, AddressRequest request);
    public void deleteAddress(String addressId);
}
