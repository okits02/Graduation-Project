package com.example.profile_service.service;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.request.ProfileUpdateRequest;
import com.example.profile_service.dto.response.CustomerVM;
import com.example.profile_service.dto.response.PageResponse;
import com.example.profile_service.dto.response.ProfileResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProfileService {
   public ProfileResponse createProfile(ProfileRequest request);
   public ProfileResponse getMyProfile();
   public ProfileResponse updateMyProfile(ProfileUpdateRequest request);
   public ProfileResponse getProfileByUserId(String userId);
   public ProfileResponse updateProfileByUserId(String userId, ProfileUpdateRequest request);
   public PageResponse<ProfileResponse> getAllProfile(int page, int size);
   public void DeleteProfile(String userId);
   public CustomerVM getProfileForRating();
   public String getUserId();
}
