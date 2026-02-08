package com.example.userservice.service;

import com.example.userservice.dto.response.*;
import com.okits02.common_lib.dto.PageResponse;
import com.example.userservice.dto.request.ForgotPasswordRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.model.Users;

import java.util.List;

public interface UserService {
    public Users createUser(UserCreationRequest request);
    public void registerVerify(String userName, String otp_code);
    public void forgotPassword(Users user, String newPassword);
    public void forgotPasswordVerify(ForgotPasswordRequest request);
    public void updatePassword(String oldPassword, String newPassword);
    public void updatePasswordForAdmin(String oldPassword, String newPassword);
    public void toggleUserStatus(String userId, boolean isActive);
    public void deleteUser(String userId);
    public UserIdResponse getUserId();
    public UserResponse getUserById(String userId);
    public UserResponse getUserByUserName(String userName);
    public UserResponse getUserByEmail(String email);
    public String getEmailByUserId(String userId);
    public UserInfoResponse getMyInfo();
    public PageResponse<UserResponse> getAll(int page, int size);
    public ListEmailResponse getListEmailByListUserId(List<String> userIds);
    public List<UserAutocompletedResponse> autocompleted(String keyword);
    public PageResponse<UserResponse> searchUserByKeyword(String email, String userName, int page, int size);
}
