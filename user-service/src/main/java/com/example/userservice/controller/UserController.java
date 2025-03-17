package com.example.userservice.controller;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.request.changePasswordRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.model.Users;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {
    private UserService userService;

    @PostMapping
    ApiResponse<Users> creationUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<Users> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("success");
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    @PutMapping("/{id}/password}")
    ResponseEntity<ApiResponse> updatePassword(@PathVariable String id, @RequestBody changePasswordRequest request) {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(id);
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("success");
        userService.updatePassword(userUpdateRequest, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/Info")
    ApiResponse<UserResponse> getMyInfo() {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("success");
        apiResponse.setResult(userService.getMyInfo());
        return apiResponse;
    }

    @GetMapping("/allUser")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = userService.getAllUsers(page, size);
        ApiResponse<Page<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("success");
        apiResponse.setResult(users);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{userid}")
    public UserResponse updateUser(@PathVariable("userid") String userId, @RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @PutMapping("/{updateMyinfo}")
    public UserResponse updateMyInfo(@RequestBody @Valid UserUpdateRequest request) {
        return userService.updateMyInfo(request);
    }

    @DeleteMapping("/{userid}/delete")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable("userid") String userId) {
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("success");
        userService.deleteUser(userId);
        return ResponseEntity.ok(apiResponse);
    }
}
