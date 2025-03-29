package com.example.userservice.controller;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.request.changePasswordRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.model.Users;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    ApiResponse<Users> creationUser(@RequestBody @Valid UserCreationRequest request) {
        try {
            return ApiResponse.<Users>builder()
                    .code(200)
                    .message("User Created")
                    .result(userService.createUser(request))
                    .build();
        }catch (AppException e) {
            return ApiResponse.<Users>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @PostMapping("/verify/{otp_code}")
    ResponseEntity<ApiResponse<?>> registerVerify(@PathVariable @Valid String otp_code, @RequestParam UserCreationRequest request)
    {
        Users user = userRepository.findById(request.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        userService.registerVerify(user, otp_code);
        return ResponseEntity.ok(ApiResponse.builder()
                        .code(200)
                        .message("User is verify")
                        .build());
    }

    @PutMapping("/reset-password")
    ResponseEntity<ApiResponse<?>> updatePassword(@RequestBody changePasswordRequest request) {
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

    @PutMapping("/{userid}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable("userid") String userId,
                                   @RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @PutMapping("/updateMyInfo")
    public UserResponse updateMyInfo(@RequestBody @Valid UserUpdateRequest request) {
        return userService.updateMyInfo(request);
    }

    @GetMapping("/Info")
    ApiResponse<UserResponse> getMyInfo() {
        try {
            return ApiResponse.<UserResponse>builder()
                    .code(200)
                    .message("OK")
                    .result(userService.getMyInfo())
                    .build();
        }catch (AppException e) {
            return ApiResponse.<UserResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
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
