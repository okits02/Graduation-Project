package com.example.profile_service.service.impl;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.mapper.ProfileMapper;
import com.example.profile_service.repository.ProfileRepository;
import com.example.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;


    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        return null;
    }

    @Override
    public ProfileResponse getMyProfile(String userId) {
        return null;
    }

    @Override
    public ProfileResponse updateMyProfile(ProfileRequest request) {
        return null;
    }

    @Override
    public void DeleteProfile(String userId) {

    }
}
