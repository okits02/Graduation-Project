package com.example.profile_service.service;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.response.AddressResponse;

public interface AddressService {
    public AddressResponse createAddress(AddressRequest request);
    public AddressResponse getMyAddress(String userId);
    public AddressResponse updateMyAddress(AddressRequest request);
    public void deleteAddress(String addressId);
}
