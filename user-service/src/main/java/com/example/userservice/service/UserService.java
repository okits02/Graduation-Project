package com.example.userservice.service;

import com.okits02.common_lib.dto.PageResponse;
import com.example.userservice.dto.request.ForgotPasswordRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.response.UserIdResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.model.Users;

public interface UserService {
    public Users createUser(UserCreationRequest request);
    public void registerVerify(String userName, String otp_code);
    public void forgotPassword(Users user, String newPassword);
    public void forgotPasswordVerify(ForgotPasswordRequest request);
    public void updatePassword(String oldPassword, String newPassword);
    public void toggleUserStatus(String userId, boolean isActive);
    public void deleteUser(String userId);
    public UserIdResponse getUserId();
    public UserResponse getUserById(String userId);
    public PageResponse<UserResponse> getAll(int page, int size);
}
