package com.example.profile_service.service;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.request.AddressUpdateRequest;
import com.example.profile_service.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    public AddressResponse createAddress(AddressRequest request);
    public List<AddressResponse> getAllMyAddress();
    public AddressResponse updateMyAddress(AddressUpdateRequest request);
    public AddressResponse getAddressByAddressId(String addressId);
    public void deleteAddress(String addressId);
}
