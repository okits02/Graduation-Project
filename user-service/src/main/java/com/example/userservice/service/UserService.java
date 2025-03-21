package com.example.userservice.service;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.model.Users;
import org.springframework.data.domain.Page;

public interface UserService {
    public Users createUser(UserCreationRequest request);
    public UserResponse updateUser(String userId, UserUpdateRequest request);
    public UserResponse updateMyInfo(UserUpdateRequest request);
    public void updatePassword(String oldPassword, String newPassword);
    public UserResponse getMyInfo();
    public UserResponse getUserById(String userId);
    public void toggleUserStatus(String userId, boolean isActive);
    public Page<UserResponse> getAllUsers(int page, int size);
    public void deleteUser(String userId);
}
