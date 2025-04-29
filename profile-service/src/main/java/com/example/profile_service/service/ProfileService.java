package com.example.profile_service.service;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.response.ProfileResponse;

public interface ProfileService {
   public ProfileResponse createProfile(ProfileRequest request);
   public ProfileResponse getMyProfile(String userId);
   public ProfileResponse updateMyProfile(ProfileRequest request);
   public void DeleteProfile(String userId);
}
