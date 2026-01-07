package com.example.profile_service.mapper;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.request.ProfileUpdateRequest;
import com.example.profile_service.dto.response.ProfileForAdminResponse;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    UserProfile toProfile(ProfileRequest request);
    ProfileResponse toProfileResponse(UserProfile userProfile);
    List<ProfileForAdminResponse> toProfileResponses(List<UserProfile> profiles);
    void updateProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest request);
}
