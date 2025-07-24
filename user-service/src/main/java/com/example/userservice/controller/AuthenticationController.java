package com.example.userservice.controller;

import com.example.userservice.dto.request.*;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.AuthenticationResponse;
import com.example.userservice.dto.response.ForgotPasswordResponse;
import com.example.userservice.dto.response.IntrospectResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.model.Users;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthenticationService;
import com.example.userservice.service.ForgotPasswordService;
import com.example.userservice.service.UserService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserRepository userRepository;
    UserService userService;
    ForgotPasswordService forgotPasswordService;

    @Operation(summary = "login",
    description = "API login for user")
    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticated(@RequestBody AuthenticationRequest request)
    {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @Operation(summary = "introspect",
    description = "API check JWT token")
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @Operation(summary = "refresh",
    description = "Api for refresh jwt token")
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> introspect(@RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @Operation(summary = "verify otp",
    description = "API is used to authenticate otp when users create new accounts")
    @PostMapping("/verify")
    ResponseEntity<ApiResponse<?>> registerVerify(@RequestBody RegisterVerifyRequest request)
    {
        Users user = userRepository.findByUsername(request.getUsername()).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_EXITS));
        userService.registerVerify(request.getUsername(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("User is verify")
                .build());
    }

    @Operation(summary = "verify otp forgot password",
            description = "API is used to authenticate otp when users forgot password")
    @PostMapping("/forgot-password")
    ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPasswordVerify(@RequestBody ForgotPasswordRequest request)
    {
        userService.forgotPasswordVerify(request);
        var result = authenticationService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<ForgotPasswordResponse>builder()
                    .result(result)
                    .build());
    }

    @Operation(summary = "logout",
            description = "API is used logout",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }
}
