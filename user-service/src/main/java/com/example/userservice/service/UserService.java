package com.example.userservice.service;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.UserIdResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.model.Users;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;

public interface UserService {
    public Users createUser(UserCreationRequest request);
    public void registerVerify(String userId, String otp_code);
    public void forgotPassword(String userId, String newPassword);
    public void updatePassword(String oldPassword, String newPassword);
    public UserResponse getUserById(String userId);
    public void toggleUserStatus(String userId, boolean isActive);
    public void deleteUser(String userId);
    public UserIdResponse getUserId();
}
