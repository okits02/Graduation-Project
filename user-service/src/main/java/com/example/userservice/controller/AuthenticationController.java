package com.example.userservice.controller;

import com.example.userservice.dto.request.AuthenticationRequest;
import com.example.userservice.dto.request.IntrospectRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.AuthenticationResponse;
import com.example.userservice.dto.response.IntrospectResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.model.Users;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthenticationService;
import com.example.userservice.service.ForgotPasswordService;
import com.example.userservice.service.UserService;
import com.nimbusds.jose.JOSEException;
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

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticated(@RequestBody AuthenticationRequest request)
    {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/verify/{otp_code}")
    ResponseEntity<ApiResponse<?>> registerVerify(@PathVariable @Valid String otp_code, @RequestBody UserUpdateRequest request)
    {
        Users user = userRepository.findById(request.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        userService.registerVerify(request.getId(), otp_code);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("User is verify")
                .build());
    }

    @PostMapping("/Forgot/{otp_code}")
    ResponseEntity<ApiResponse<?>> ForgotPasswordVerify(@PathVariable @Valid String otp_code, @RequestParam String userId)
    {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        userService.forgotPasswordVerify(userId, otp_code);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Otp is verify")
                .build());
    }
}
