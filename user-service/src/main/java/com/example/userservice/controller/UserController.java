package com.example.userservice.controller;

import com.example.userservice.dto.request.*;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserIdResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.kafka.CreateProfileEvent;
import com.example.userservice.kafka.NotificationEvent;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.ForgotPassword;
import com.example.userservice.model.OTP;
import com.example.userservice.model.Users;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.ForgotPasswordService;
import com.example.userservice.service.UserService;
import com.example.userservice.service.VerificationService;
import com.example.userservice.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final ForgotPasswordService forgotPasswordService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    ApiResponse<UserResponse> creationUser(@RequestBody @Valid UserCreationRequest request) {
        Users users = userService.createUser(request);
        CreateProfileEvent createProfileEvent = CreateProfileEvent.builder()
                .userId(users.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();
        kafkaTemplate.send("create-profile", createProfileEvent).whenComplete(
                (result, ex) -> {
            if (ex != null)
            {
                System.err.println("Failed to send message" + ex.getMessage());
            } else {
                System.err.println("send message successfully" + result.getProducerRecord());
            }
        });
        try {
            return ApiResponse.<UserResponse>builder()
                    .code(200)
                    .message("User Created")
                    .result(userMapper.toUserResponse(users))
                    .build();
        }catch (AppException e) {
            return ApiResponse.<UserResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }


    @PostMapping("/verifyEmail/send-otp")
    ApiResponse<?> endVerificationOTP(@RequestBody @Valid UserUpdateRequest request)
    {
        Optional<Users> users = userRepository.findById(request.getId());
        verificationService.sendverifyOtp(users.get());
        Optional<OTP> otp = verificationService.getOtpByUserId(request.getId());
        if(otp.isEmpty())
        {
            throw new AppException(ErrorCode.OTP_NOT_EXISTS);
        }
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(users.get().getEmail())
                .content(otp.get().getOtp_code())
                .build();
        kafkaTemplate.send("send-otp", notificationEvent).whenComplete(
                (result, ex) -> {
            if (ex != null)
            {
                System.err.println("Failed to send message" + ex.getMessage());
            } else {
              System.err.println("send message successfully" + result.getProducerRecord());
            }
        });
        return ApiResponse.builder()
                .code(200)
                .message("OTP has been sent to your email!")
                .build();
    }

    @PostMapping("/forgot-password/send-otp")
    ApiResponse<?> sendForgotPasswordOTP(@RequestBody @Valid ForgotPasswordRequest request)
    {
        Optional<Users> users = userRepository.findByEmail(request.getEmail());
        forgotPasswordService.createOTP(users.get(), OtpUtils.generateOtp(), request.getEmail());
        Optional<ForgotPassword> forgotPassword = forgotPasswordService.findByUserId(users.get().getId());
        if(users.isEmpty())
        {
            throw new AppException(ErrorCode.USER_NOT_EXITS);
        }
        if(forgotPassword.isEmpty())
        {
            throw new AppException(ErrorCode.OTP_NOT_EXISTS);
        }
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("Email")
                .recipient(request.getEmail())
                .content(forgotPassword.get().getOtp_code())
                .build();
        kafkaTemplate.send("send-otp", notificationEvent).whenComplete(
                (result, ex) ->{
            if(ex!=null)
            {
                System.err.println("Failed to send event " +ex.getMessage());
            }else
            {
                System.err.println("send message successfully " + result.getProducerRecord());
            }
        });
        return ApiResponse.builder()
                .code(200)
                .message("OTP has been sent to your email")
                .build();
    }

    @PutMapping("/forgot-password")
    ResponseEntity<ApiResponse<?>> forgotPassword(@RequestBody ChangePasswordRequest request)
    {
        try{
            Users users = userRepository.findByEmail(request.getEmail()).orElseThrow(
                    () -> new AppException(ErrorCode.USER_NOT_EXITS));
            userService.forgotPassword(users.getId(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("Password update successfully")
                    .build());
        } catch (AppException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .code(500)
                    .message("Internal server error")
                    .build());
        }
    }

    @PutMapping("/reset-password")
    ResponseEntity<ApiResponse<?>> updatePassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.updatePassword(request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Password updated successfully")
                .build());

        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                .code(500)
                .message("Internal server error")
                .build());
        }
    }

    @GetMapping("/getUserId")
    ResponseEntity<ApiResponse<UserIdResponse>> getUserId()
    {
        return ResponseEntity.ok(ApiResponse.<UserIdResponse>builder()
                .code(200)
                .result(userService.getUserId())
                .build());
    }

    @PutMapping("/{userid}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable("userid") String userId,
                                   @RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId) {
        try {
            return ApiResponse.<UserResponse>builder()
                    .code(200)
                    .message("OK")
                    .result(userService.getUserById(userId))
                    .build();
        }catch (AppException e) {
            return ApiResponse.<UserResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> toggleUserStatus(@PathVariable String userId,
                                                           @RequestParam boolean isActive) {
        try {
            userService.toggleUserStatus(userId, isActive);

            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("User status updated successfully")
                    .build());

        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .code(500)
                    .message("Internal server error")
                    .build());
        }
    }

    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.<Page<UserResponse>>builder()
                    .code(400)
                    .message("Page index must be non-negative and size must be greater than zero")
                    .build());
        }

        Page<UserResponse> users = userService.getAllUsers(page, size);

        return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
                .code(1000)
                .message("success")
                .result(users)
                .build());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.builder()
                    .code(204)
                    .message("User deleted successfully")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }
}
