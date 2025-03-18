package com.example.userservice.service;

import com.example.userservice.constant.PredefinedRole;
import com.example.userservice.dto.request.AdminCreateUserRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.Role;
import com.example.userservice.model.Users;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import jakarta.persistence.GeneratedValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;

public interface UserService {
    public Users createUser(UserCreationRequest request);
    public UserResponse updateUser(String userId, UserUpdateRequest request);
    public UserResponse updateMyInfo(UserUpdateRequest request);
    public void updatePassword(UserUpdateRequest request, String oldPassword, String newPassword);
    public UserResponse getMyInfo();
    public UserResponse getUserById(String userId);
    public void toggleUserStatus(String userId, boolean isActive);
    public Page<UserResponse> getAllUsers(int page, int size);
    public void deleteUser(String userId);
}
