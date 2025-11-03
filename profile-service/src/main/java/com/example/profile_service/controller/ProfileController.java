package com.example.profile_service.controller;

import com.example.profile_service.dto.request.ProfileRequest;
import com.example.profile_service.dto.request.ProfileUpdateRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.profile_service.dto.response.CustomerVM;
import com.okits02.common_lib.dto.PageResponse;
import com.example.profile_service.dto.response.ProfileResponse;
import com.example.profile_service.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {

    ProfileService profileService;

    @Operation(summary = "get my info",
            description = "Api used to get user's info",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "update info",
            description = "Api used to update user's info",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "admin get user info",
            description = "Api used to get user's info",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "admin update user info",
            description = "Api used to update user's info",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "admin delete user info",
            description = "Api used to delete user's info",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteMyProfile(@PathVariable String userId) {
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Profile deleted successfully")
                .result(null)
                .build();
    }

    @Operation(summary = "admin get all user info",
            description = "Api used to get all user's info",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin/getAll")
    @PreAuthorize("hasRole('ADMIN)")
    public ResponseEntity<ApiResponse<PageResponse<ProfileResponse>>> getAllProfile(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    )
    {
        return ResponseEntity.ok(ApiResponse.<PageResponse<ProfileResponse>>builder()
                        .result(profileService.getAllProfile(page, size))
                .build());
    }

    @GetMapping("/rating/getProfile")
    public ResponseEntity<ApiResponse<CustomerVM>> getProfileForRating(){
        return ResponseEntity.ok(ApiResponse.<CustomerVM>builder()
                        .result(profileService.getProfileForRating())
                        .code(200)
                .build());
    }
}
