package com.example.userservice.service;

import com.example.userservice.constant.PredefinedRole;
import com.example.userservice.dto.request.UserCreationRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper  userMapper;
    PasswordEncoder passwordEncoder;

    public Users createUser(UserCreationRequest request)
    {
        if(userRepository.existsByUsername(request.getUsername()))
        {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role roles = roleRepository.findById(PredefinedRole.USER_ROLE).orElseThrow(()
                ->new AppException(ErrorCode.ROLE_NOT_EXISTS));
        user.setRole(roles);
        return userRepository.save(user);
    }
}
