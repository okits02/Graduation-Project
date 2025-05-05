package com.example.profile_service.mapper;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    UserProfile toProfile(ProfileRequest request);
    ProfileResponse toProfileResponse(UserProfile userProfile);
    void updateProfile(@MappingTarget UserProfile userProfile, ProfileRequest request);
}
