package com.example.profile_service.controller;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.request.ProfileUpdateRequest;
import com.example.profile_service.dto.response.ApiResponse;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {

    ProfileService profileService;

    @GetMapping("/myInfo")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {
        ProfileResponse profile = profileService.getMyProfile();
        return ResponseEntity.ok(
                ApiResponse.<ProfileResponse>builder()
                        .code(200)
                        .message("Profile retrieved successfully")
                        .result(profile)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(@RequestBody @Valid ProfileUpdateRequest request) {
        ProfileResponse updatedProfile = profileService.updateMyProfile(request);
        return ResponseEntity.ok(
                ApiResponse.<ProfileResponse>builder()
                        .code(200)
                        .message("Profile updated successfully")
                        .result(updatedProfile)
                        .build()
        );
    }

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(@PathVariable String userId)
    {
        ProfileResponse profileResponse = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<ProfileResponse>builder()
                        .code(200)
                        .message("Profile retrieved successfully!")
                        .result(profileResponse)
                .build());
    }

    @PutMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfileByUserId(@PathVariable String userId,
                                                                              @RequestBody ProfileUpdateRequest request)
    {
        ProfileResponse profileResponse = profileService.updateProfileByUserId(userId, request);
        return ResponseEntity.ok(ApiResponse.<ProfileResponse>builder()
                        .code(200)
                        .message("Profile update successfully!")
                        .result(profileResponse)
                .build());
    }

    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMyProfile(@PathVariable String userId) {
        profileService.DeleteProfile(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.<Void>builder()
                        .code(204)
                        .message("Profile deleted successfully")
                        .result(null)
                        .build()
        );
    }

    @GetMapping("/admin/getAll")
    @PreAuthorize("hasRole('ADMIN)")
    public ResponseEntity<ApiResponse<Page<ProfileResponse>>> getAllProfile(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        return ResponseEntity.ok(ApiResponse.<Page<ProfileResponse>>builder()
                        .result(profileService.getAllProfile(page, size))
                .build());
    }
}
