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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {
    ProfileService profileService;

    @PostMapping
    ApiResponse<ProfileResponse> createProfile(@RequestBody @Valid ProfileRequest request)
    {
        return ApiResponse.<ProfileResponse>builder()
                .code(200)
                .message("Create successful profile!")
                .result(profileService.createProfile(request))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<ProfileResponse> getMyProfile(@PathVariable String userId)
    {
        return ApiResponse.<ProfileResponse>builder()
                .code(200)
                .result(profileService.getMyProfile(userId))
                .build();
    }

    @PutMapping
    ApiResponse<ProfileResponse> updateMyProfile(@RequestBody @Valid ProfileRequest request)
    {
        return ApiResponse.<ProfileResponse>builder()
                .code(200)
                .result(profileService.updateMyProfile(request))
                .build();
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<ApiResponse<?>> deleteMyProfile(@PathVariable String userId)
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.builder()
            .code(204)
            .message("User deleted successfully")
            .build());
    }
}
