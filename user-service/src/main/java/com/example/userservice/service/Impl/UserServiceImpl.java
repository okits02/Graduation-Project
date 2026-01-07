package com.example.userservice.service.Impl;

import com.example.userservice.constant.PredefinedRole;
import com.example.userservice.dto.response.*;
import com.example.userservice.exception.UserErrorCode;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import com.example.userservice.dto.request.ForgotPasswordRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.okits02.common_lib.exception.AppException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.ForgotPassword;
import com.example.userservice.model.OTP;
import com.example.userservice.model.Role;
import com.example.userservice.model.Users;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.httpClient.ProfileClient;
import com.example.userservice.service.ForgotPasswordService;
import com.example.userservice.service.UserService;
import com.example.userservice.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ProfileClient profileClient;
    static final long OTP_VALID_TIME = 5 * 60 * 1000;


    @Override
    public Users createUser(UserCreationRequest request) {
        if(userRepository.existsByUsername(request.getUsername()))
        {
            throw new AppException(UserErrorCode.USER_EXISTS);
        }
        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new AppException(UserErrorCode.EMAIL_EXISTS);
        }
        Users user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        log.info("createUser: {}", user);
        Role roles = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(PredefinedRole.USER_ROLE);
                    return roleRepository.save(newRole);
                });
        user.setRole(roles);
        user.setActive(true);
        user.setVerified(false);
        userRepository.save(user);
        return user;
    }

    @Override
    public void registerVerify(String userName, String otp_code) {
        Users users = userRepository.findByUsername(userName).orElseThrow(
                () -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        Optional<OTP> optional = verificationService.getOtpByUserId(users.getId());
        OTP otp = optional.orElseThrow(() -> new AppException(UserErrorCode.OTP_NOT_EXISTS));
        long currentTimeInMillis = System.currentTimeMillis();
        long otpRequestTimeInMillis = otp.getOtp_request_time().getTime();
        if(otpRequestTimeInMillis + OTP_VALID_TIME < currentTimeInMillis)
        {
            throw new AppException(UserErrorCode.OTP_EXPIRED);
        }
        if(!otp.getOtp_code().equals(otp_code))
        {
            throw new AppException(UserErrorCode.OTP_INVALID);
        }
        users.setVerified(true);
        userRepository.save(users);
        verificationService.deleteOtpById(otp);
    }

    @Override
    public void forgotPasswordVerify(ForgotPasswordRequest request) {
        Users users = userRepository.findByEmail(request.getEmail()).orElseThrow(()
                -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        Optional<ForgotPassword> forgotPassword = forgotPasswordService.findByUserId(users.getId());
        long currentTimeInMillis = System.currentTimeMillis();
        long otpRequestTimeInMillis = forgotPassword.get().getOtp_request_time().getTime();
        if(otpRequestTimeInMillis + OTP_VALID_TIME < currentTimeInMillis)
        {
            throw new AppException(UserErrorCode.OTP_EXPIRED);
        }
        if(!forgotPassword.get().getOtp_code().equals(request.getOtp()))
        {
            throw new AppException(UserErrorCode.OTP_INVALID);
        }
        forgotPasswordService.deleteOTP(forgotPassword.get().getId());
    }

    @Override
    public void forgotPassword(Users user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUsername).orElseThrow(
                () -> new AppException(UserErrorCode.USER_NOT_EXISTS));

        if(!passwordEncoder.matches(oldPassword,users.getPassword()))
        {
            throw new AppException(UserErrorCode.PASSWORD_NOT_MATCH);
        }
        users.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(users);
    }

    @Override
    public void updatePasswordForAdmin(String oldPassword, String newPassword) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUsername).orElseThrow(
                () -> new AppException(UserErrorCode.USER_NOT_EXISTS));

        if(!passwordEncoder.matches(oldPassword,users.getPassword()))
        {
            throw new AppException(UserErrorCode.PASSWORD_NOT_MATCH);
        }
        users.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(users);
    }

    @Override
    public UserResponse getUserById(String userId) {
        Optional<Users> user = userRepository.findById(userId);
        if(user.isEmpty())
        {
            throw new AppException(UserErrorCode.USER_NOT_EXISTS);
        }
        UserResponse response = userMapper.toUserResponse(user.get());

        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        ApiResponse<ProfileResponse> profileResponse =
                profileClient.getUserProfile(authHeader, userId);
        if (profileResponse != null && profileResponse.getCode() == 200) {
            enrichUserWithProfile(response, profileResponse.getResult());
        }
        return response;
    }

    @Override
    public void toggleUserStatus(String userId, boolean isActive) {
        Users users = userRepository.findById(userId).orElseThrow(()
                -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        users.setActive(isActive);
        userRepository.save(users);
    }

    @Override
        public void deleteUser(String userId) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
            var response = profileClient.deleteMyProfile(authHeader, userId);
            if (response == null || response.getCode() != 200) {
                throw new RuntimeException("Failed to delete profile from Profile-service");
            }
            userRepository.deleteById(userId);
        }

    @Override
    public UserIdResponse getUserId() {
        var contex = SecurityContextHolder.getContext();
        String currentUsername = contex.getAuthentication().getName();
        Users users = userRepository.findByUsername(currentUsername).orElseThrow(()
                -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        return UserIdResponse.builder()
                .userId(users.getId())
                .build();
    }

    @Override
    public PageResponse<UserForAdminResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = userRepository.getAll(pageable);
        List<UserForAdminResponse> users =
                pageData.getContent()
                        .stream()
                        .map(userMapper::toUserForAdminResponse)
                        .toList();

        if (users.isEmpty()) {
            return PageResponse.<UserForAdminResponse>builder()
                    .currentPage(page)
                    .pageSize(size)
                    .totalElements(pageData.getTotalElements())
                    .data(List.of())
                    .build();
        }
        List<String> userIds = pageData.getContent()
                .stream()
                .map(Users::getId)
                .toList();
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var profileResponse = profileClient.getListUserProfile(authHeader, userIds);
        if (profileResponse == null || profileResponse.getCode() != 200) {
            log.warn("Failed to fetch profiles, return users without profile");
            return PageResponse.<UserForAdminResponse>builder()
                    .currentPage(page)
                    .pageSize(size)
                    .totalElements(pageData.getTotalElements())
                    .data(users)
                    .build();
        }
        Map<String, ProfileResponse> profileMap =
                profileResponse.getResult()
                        .stream()
                        .collect(Collectors.toMap(
                                ProfileResponse::getUserId,
                                p -> p
                        ));

        users.forEach(user -> {
            ProfileResponse profile = profileMap.get(user.getId());
            if (profile != null) {
                mergeProfile(user, profile);
            }
        });

        return PageResponse.<UserForAdminResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements(pageData.getTotalElements())
                .data(users)
                .build();
    }

    @Override
    public ListEmailResponse getListEmailByListUserId(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return ListEmailResponse.builder()
                    .emails(List.of())
                    .build();
        }

        List<String> emails =
                userRepository.findEmailsByUserIds(userIds);

        return ListEmailResponse.builder()
                .emails(emails)
                .build();
    }
    private void enrichUserWithProfile(
            UserResponse userResponse,
            ProfileResponse profile
    ) {
        if (profile == null) return;

        userResponse.setAvatarUrl(profile.getAvatarUrl());
        userResponse.setFirstName(profile.getFirstName());
        userResponse.setLastName(profile.getLastName());
        userResponse.setPhone(profile.getPhone());
        userResponse.setDob(profile.getDob());
        userResponse.setAddress(profile.getAddress());
    }

    private void mergeProfile(UserForAdminResponse user, ProfileResponse profile) {

        user.setAvatarUrl(profile.getAvatarUrl());
        user.setFirstName(profile.getFirstName());
        user.setLastName(profile.getLastName());
        user.setPhone(profile.getPhone());
        user.setDob(profile.getDob());
        user.setAddress(profile.getAddress());
    }
}
