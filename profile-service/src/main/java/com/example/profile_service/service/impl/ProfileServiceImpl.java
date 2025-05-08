package com.example.profile_service.service.impl;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.entity.UserProfile;
import com.example.profile_service.exception.AppException;
import com.example.profile_service.exception.ErrorCode;
import com.example.profile_service.mapper.ProfileMapper;
import com.example.profile_service.repository.ProfileRepository;
import com.example.profile_service.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;


    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserProfile userProfile = profileRepository.findByUserId(request.getUserId());
        if (userProfile != null)
        {
            throw new AppException(ErrorCode.PROFILE_EXISTS);
        }
        UserProfile userProfile1 = profileMapper.toProfile(request);
        profileRepository.save(userProfile1);
        return profileMapper.toProfileResponse(userProfile1);
    }

    @Override
    public ProfileResponse getMyProfile(String userId) {
        UserProfile userProfile = profileRepository.findByUserId(userId);
        if(userProfile == null)
        {
            throw new AppException(ErrorCode.PROFILE_NOT_EXITS);
        }
        return profileMapper.toProfileResponse(userProfile);
    }

    @Override
    public ProfileResponse updateMyProfile(ProfileRequest request) {
        UserProfile userProfile = profileRepository.findByUserId(request.getUserId());
        if(userProfile == null)
        {
            throw new AppException(ErrorCode.PROFILE_NOT_EXITS);
        }
        profileMapper.updateProfile(userProfile, request);
        return profileMapper.toProfileResponse(profileRepository.save(userProfile));
    }

    @Override
    public void DeleteProfile(String userId) {
        profileRepository.deleteByUserId(userId);
    }
}
