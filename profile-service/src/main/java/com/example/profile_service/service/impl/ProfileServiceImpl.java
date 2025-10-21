package com.example.profile_service.service.impl;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.request.ProfileUpdateRequest;
import com.example.profile_service.dto.response.*;
import com.example.profile_service.entity.UserAddress;
import com.example.profile_service.entity.UserProfile;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.example.profile_service.exception.ProfileErrorCode;
import com.example.profile_service.mapper.ProfileMapper;
import com.example.profile_service.repository.AddressRepository;
import com.example.profile_service.repository.ProfileRepository;
import com.example.profile_service.repository.httpClient.UserServiceClient;
import com.example.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.dto.ApiResponse;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    UserServiceClient userServiceClient;
    AddressRepository addressRepository;


    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserProfile userProfile = profileRepository.findByUserId(request.getUserId());
        if (userProfile != null)
        {
            throw new AppException(ProfileErrorCode.PROFILE_EXISTS);
        }
        UserProfile userProfile1 = profileMapper.toProfile(request);
        profileRepository.save(userProfile1);
        return profileMapper.toProfileResponse(userProfile1);
    }

    @Override
    public ProfileResponse getMyProfile() {
        String userId = getUserId();
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null)
        {
            throw new AppException(ProfileErrorCode.PROFILE_NOT_EXITS);
        }
        return profileMapper.toProfileResponse(userProfile);
    }

    @Override
    public ProfileResponse updateMyProfile(ProfileUpdateRequest request) {
        String userId = getUserId();
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null)
        {
            throw new AppException(ProfileErrorCode.PROFILE_NOT_EXITS);
        }
        profileMapper.updateProfile(userProfile, request);
        return profileMapper.toProfileResponse(profileRepository.save(userProfile));
    }

    @Override
    public ProfileResponse getProfileByUserId(String userId) {
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null)
        {
            throw  new AppException(ProfileErrorCode.PROFILE_NOT_EXITS);
        }
        return profileMapper.toProfileResponse(userProfile);
    }

    @Override
    public ProfileResponse updateProfileByUserId(String userId, ProfileUpdateRequest request) {
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null)
        {
            throw new AppException(ProfileErrorCode.PROFILE_NOT_EXITS);
        }
        profileMapper.updateProfile(userProfile, request);
        return profileMapper.toProfileResponse(profileRepository.save(userProfile));
    }

    @Override
    public PageResponse<ProfileResponse> getAllProfile(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = profileRepository.findAll(pageable);
        return PageResponse.<ProfileResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(profileMapper::toProfileResponse).toList())
                .build();
    }

    @Override
    public void DeleteProfile(String userId) {
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if (userProfile == null) {
            throw new AppException(ProfileErrorCode.PROFILE_NOT_EXITS);
        }

        List<UserAddress> addressList = userProfile.getAddress();
        if(addressList != null || !addressList.isEmpty()) {
            for (UserAddress address : addressList) {
                addressRepository.deleteById(address.getId());
            }
        }
        userProfile.setAddress(null);
        profileRepository.deleteById(userProfile.getId());
    }

    @Override
    public CustomerVM getProfileForRating() {
        String userId = getUserId();
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null){
            throw new AppException(ProfileErrorCode.PROFILE_NOT_EXITS);
        }
        CustomerVM customerVM = CustomerVM.builder()
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .build();
        return customerVM;
    }

    @Override
    public String getUserId() {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        log.info("Header: {}", authHeader);
        ApiResponse<GetUserIdResponse> apiResponse = userServiceClient.getUserId(authHeader);
        return apiResponse.getResult().getUserId();
    }
}
