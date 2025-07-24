package com.example.userservice.service.Impl;

import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.model.ForgotPassword;
import com.example.userservice.model.Users;
import com.example.userservice.repository.ForgotPasswordRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.ForgotPasswordService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class  ForgotPasswordServiceImpl implements ForgotPasswordService {
    ForgotPasswordRepository forgotPasswordRepository;
    UserRepository userRepository;
    @Override
    public ForgotPassword createOTP(Users users, String otp, String sendTo) {
        forgotPasswordRepository.findByUserId(users.getId()).ifPresent(forgotPasswordRepository::delete);
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .user(users)
                .otp_code(otp)
                .sendTo(sendTo)
                .otp_request_time(new Date())
                .build();
        return forgotPasswordRepository.save(forgotPassword);
    }

    @Override
    public ForgotPassword findById(String id) {
        return forgotPasswordRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.OTP_NOT_EXISTS));
    }

    @Override
    public Optional<ForgotPassword> findByUserId(String userId) {
        return forgotPasswordRepository.findByUserId(userId);
    }

    @Override
    public void deleteOTP(String id) {
        forgotPasswordRepository.deleteById(id);
    }
}
