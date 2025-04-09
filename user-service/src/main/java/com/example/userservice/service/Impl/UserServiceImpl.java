package com.example.userservice.service.Impl;

import com.example.userservice.constant.PredefinedRole;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.ForgotPassword;
import com.example.userservice.model.OTP;
import com.example.userservice.model.Role;
import com.example.userservice.model.Users;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.ForgotPasswordService;
import com.example.userservice.service.UserService;
import com.example.userservice.service.VerificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
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
    private final VerificationService verificationService;
    private final ForgotPasswordService forgotPasswordService;
    static final long OTP_VALID_TIME = 5 * 60 * 1000;


    @Override
    public Users createUser(UserCreationRequest request) {
        if(userRepository.existsByUsername(request.getUsername()))
        {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        log.info("createUser: {}", user);
        Role roles = roleRepository.findById(PredefinedRole.USER_ROLE)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(PredefinedRole.USER_ROLE);
                    return roleRepository.save(newRole);
                });
        user.setRole(roles);
        user.setActive(true);
        user.setVerified(false);
        userRepository.save(user);
        verificationService.sendverifyOtp(user);
        return user;
    }

    @Override
    public void registerVerify(String userId, String otp_code) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        Optional<OTP> optional = verificationService.getOtpByUserId(userId);
        OTP otp = optional.orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTS));
        long currentTimeInMillis = System.currentTimeMillis();
        long otpRequestTimeInMillis = otp.getOtp_request_time().getTime();
        if(otpRequestTimeInMillis + OTP_VALID_TIME < currentTimeInMillis)
        {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        if(!otp.getOtp_code().equals(otp_code))
        {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        users.setVerified(true);
        userRepository.save(users);
        verificationService.deleteOtpById(otp);
    }

    @Override
    public void forgotPasswordVerify(String userId, String otp_code) {
        Users users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITS));
        Optional<ForgotPassword> forgotPassword = forgotPasswordService.findByUserId(userId);
        ForgotPassword forgotPassword1 = forgotPassword.get();
        long currentTImeInMillis = System.currentTimeMillis();
        long otpRequestTimeInMillis = forgotPassword1.getOtp_request_time().getTime();
        if(otpRequestTimeInMillis + OTP_VALID_TIME < currentTImeInMillis)
        {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        if(!forgotPassword1.getOtp_code().equals(otp_code))
        {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        forgotPasswordService.deleteOTP(forgotPassword1.getId());
    }

    @Override
    public void forgotPassword(String newPassword) {
        var context = SecurityContextHolder.getContext();
        String currentUserName = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUserName).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXITS));
        users.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(users);
    }


    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        Users users = userRepository.findById(String.valueOf(userId)).orElseThrow(()->
                new AppException(ErrorCode.USER_NOT_EXITS));
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
