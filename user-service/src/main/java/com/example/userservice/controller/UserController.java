package com.example.userservice.controller;

import com.example.userservice.exception.UserErrorCode;
import com.okits02.common_lib.dto.PageResponse;
import com.example.userservice.dto.request.*;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.userservice.dto.response.UserIdResponse;
import com.example.userservice.dto.response.UserResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
@Tag(name = "Api for user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final ForgotPasswordService forgotPasswordService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Operation(summary = "create user", description = "Api creates users for new people")
    @PostMapping("/register")
    ApiResponse<UserResponse> creationUser(@RequestBody @Valid UserCreationRequest request) {
        Users users = userService.createUser(request);
        CreateProfileEvent createProfileEvent = CreateProfileEvent.builder()
                .userId(users.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .dob(request.getDob())
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
        UserResponse userResponse = UserResponse.builder()
                .username(users.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(users.getEmail())
                .phone(request.getPhone())
                .role(users.getRole())
                .build();
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("User Created")
                .result(userResponse)
                .build();
    }

    @Operation(summary = "send otp",
            description = "API is used to send OTP via email of users, OTP is used to verify email")
    @PostMapping("/verifyEmail/send-otp")
    ApiResponse<?> sendVerificationOTP(@RequestBody @Valid UserCreationRequest request)
    {
        Users users = userRepository.findByUsername(request.getUsername()).orElseThrow(()
                -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        verificationService.sendverifyOtp(users);
        Optional<OTP> otp = verificationService.getOtpByUserId(users.getId());
        if(otp.isEmpty())
        {
            throw new AppException(UserErrorCode.OTP_NOT_EXISTS);
        }
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(users.getEmail())
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

    @Operation(summary = "send otp when forgot password",
            description = "API is used to send OTP via email of users, OTP is used to verify and change the password")
    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<?> sendForgotPasswordOTP(@RequestBody @Valid ForgotPasswordRequest request) {
        Optional<Users> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            throw new AppException(UserErrorCode.USER_NOT_EXISTS);
        }
        Users user = optionalUser.get();

        String otpCode = OtpUtils.generateOtp();
        forgotPasswordService.createOTP(user, otpCode, request.getEmail());

        Optional<ForgotPassword> optionalForgotPassword = forgotPasswordService.findByUserId(user.getId());
        if (optionalForgotPassword.isEmpty()) {
            throw new AppException(UserErrorCode.OTP_NOT_EXISTS);
        }

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("Email")
                .recipient(request.getEmail())
                .content(optionalForgotPassword.get().getOtp_code())
                .build();

        kafkaTemplate.send("send-otp", notificationEvent).whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("Failed to send event: " + ex.getMessage());
            } else {
                System.out.println("Sent OTP event: " + result.getProducerRecord());
            }
        });

        return ApiResponse.builder()
                .code(200)
                .message("OTP has been sent to your email")
                .build();
    }
  
    @Operation(summary = "forgot password",
    description = "change new password after verify email by OTP")
    @PutMapping("/forgot-password")
    ResponseEntity<ApiResponse<?>> forgotPassword(@RequestBody ChangePasswordRequest request)
    {
        try{
            var contex = SecurityContextHolder.getContext();
            String email = contex.getAuthentication().getName();
            Users users = userRepository.findByEmail(email).orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
            log.info("user: {}", users);
            userService.forgotPassword(users, request.getNewPassword());
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

    @Operation(summary = "reset password",
            description = "API used when users want to change passwords when logging in",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "get userId",
            description = "API used to get userId",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/getUserId")
    ResponseEntity<ApiResponse<UserIdResponse>> getUserId()
    {
        return ResponseEntity.ok(ApiResponse.<UserIdResponse>builder()
                .code(200)
                .result(userService.getUserId())
                .build());
    }

    @Operation(summary = "Toggle user status admin",
    description = "API for administrator, it has an exaggeration of user account",
    security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "delete user admin",
            description = "API for administrators, it has the effect of deleting the user's account",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping("/get-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable String userId){
        try{
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("get user successfully!")
                    .result(userService.getUserById(userId))
                    .build());
        }catch (AppException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getAllUser(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){

        return ResponseEntity.ok(ApiResponse.<PageResponse<?>>builder()
                .code(200)
                .message("get all users information successfully!")
                .result(userService.getAll(page - 1
                        , size))
                .build());
    }
}
