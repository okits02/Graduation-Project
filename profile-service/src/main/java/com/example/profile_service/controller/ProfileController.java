package com.example.profile_service.controller;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.response.ApiResponse;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {

    ProfileService profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@PathVariable String userId) {
        ProfileResponse profile = profileService.getMyProfile(userId);
        return ResponseEntity.ok(
                ApiResponse.<ProfileResponse>builder()
                        .code(200)
                        .message("Profile retrieved successfully")
                        .result(profile)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(@RequestBody @Valid ProfileRequest request) {
        ProfileResponse updatedProfile = profileService.updateMyProfile(request);
        return ResponseEntity.ok(
                ApiResponse.<ProfileResponse>builder()
                        .code(200)
                        .message("Profile updated successfully")
                        .result(updatedProfile)
                        .build()
        );
    }

    @DeleteMapping("/{userId}")
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
}
