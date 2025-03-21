package com.example.userservice.service.Impl;

import com.example.userservice.constant.PredefinedRole;
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
import com.example.userservice.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Users createUser(UserCreationRequest request) {
        if(userRepository.existsByUsername(request.getUsername()))
        {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        log.info("createUser: {}", user);
        Role roles = roleRepository.findById(PredefinedRole.USER_ROLE).orElseThrow(()
                ->new AppException(ErrorCode.ROLE_NOT_EXISTS));
        user.setRole(roles);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        Users users = userRepository.findById(String.valueOf(userId)).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXITS));
        userMapper.updateUser(users, request);
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        var role = roleRepository.findAllById(request.getRole());
        users.setRole(role);
        return userMapper.toUserResponse(userRepository.save(users));
    }

    @Override
    public UserResponse updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUsername).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTS));
        userMapper.updateUser(users, request);
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(users));
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUsername).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));

        if(!passwordEncoder.matches(oldPassword,users.getPassword()))
        {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        users.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(users);
    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUsername).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTS));
        return userMapper.toUserResponse(users);
    }

    @Override
    public UserResponse getUserById(String userId) {
        Optional<Users> user = userRepository.findById(userId);
        if(user.isEmpty())
        {
            throw new AppException(ErrorCode.USER_NOT_EXITS);
        }
        return userMapper.toUserResponse(user);
    }

    @Override
    public void toggleUserStatus(String userId, boolean isActive) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        users.setActive(isActive);
        userRepository.save(users);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

}
