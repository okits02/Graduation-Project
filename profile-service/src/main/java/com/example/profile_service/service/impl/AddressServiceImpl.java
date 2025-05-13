package com.example.profile_service.service.impl;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.response.AddressResponse;
import com.example.profile_service.entity.UserAddress;
import com.example.profile_service.entity.UserProfile;
import com.example.profile_service.exception.AppException;
import com.example.profile_service.exception.ErrorCode;
import com.example.profile_service.mapper.AddressMapper;
import com.example.profile_service.repository.AddressRepository;
import com.example.profile_service.repository.ProfileRepository;
import com.example.profile_service.service.AddressService;
import com.example.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final ProfileRepository profileRepository;
    private final AddressMapper addressMapper;
    private final ProfileService profileService;

    @Override
    public AddressResponse createAddress(AddressRequest request) {
        String userId = profileService.getUserId();
        UserProfile userProfile = profileRepository.findByUserId(userId);
        UserAddress userAddress = addressMapper.toAddress(request);
        List<UserAddress> userAddressList = userProfile.getAddress();
        userAddressList.add(userAddress);
        userProfile.setAddress(userAddressList);
        profileRepository.save(userProfile);
        addressRepository.save(userAddress);
        return addressMapper.toAddressResponse(userAddress);
    }

    @Override
    public List<AddressResponse> getAllMyAddress() {
        String userId = profileService.getUserId();
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null)
        {
            throw new AppException(ErrorCode.PROFILE_NOT_EXITS);
        }
        List<UserAddress> userAddressList = userProfile.getAddress();
        return addressMapper.toListAddress(userAddressList);
    }

    @Override
    public AddressResponse updateMyAddress(AddressRequest request) {
        String userId = profileService.getUserId();
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if (userProfile == null) {
            throw new AppException(ErrorCode.PROFILE_NOT_EXITS);
        }

        List<UserAddress> userAddressList = userProfile.getAddress();
        UserAddress updatedAddress = null;

        for (UserAddress address : userAddressList) {
            if (address.getId().equals(request.getId())) {
                addressMapper.updateAddress(address, request);
                updatedAddress = address;
                break;
            }
        }

        if (updatedAddress == null) {
            throw new AppException(ErrorCode.ADDRESS_NOT_EXITS);
        }

        profileRepository.save(userProfile);
        addressRepository.save(updatedAddress);   

        return addressMapper.toAddressResponse(updatedAddress);
    }

    @Override
    public void deleteAddress(String addressId) {
        profileRepository.deleteById(addressId);
    }
}
