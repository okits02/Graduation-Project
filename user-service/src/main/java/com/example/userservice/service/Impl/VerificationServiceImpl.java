package com.example.userservice.service.Impl;

import com.example.userservice.exception.AppException;
import com.example.userservice.exception.ErrorCode;
import com.example.userservice.model.OTP;
import com.example.userservice.model.Users;
import com.example.userservice.repository.OtpRepository;
import com.example.userservice.service.VerificationService;
import com.example.userservice.utils.OtpUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationServiceImpl implements VerificationService {
    OtpRepository otpRepository;
    @Override
    public OTP sendverifyOtp(Users users) {
        otpRepository.findByUserId(users.getId()).ifPresent(otpRepository::delete);
        OTP otp = OTP.builder()
                .otp_code(OtpUtils.generateOtp())
                .otp_request_time(new Date())
                .user(users)
                .build();
        otpRepository.save(otp);
        return otp;
    }

    @Override
    public OTP getOtpById(String otpId) {
        Optional<OTP> otp = Optional.ofNullable(otpRepository.findById(otpId)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTS)));
        return otp.get();
    }

    @Override
    public Optional<OTP> getOtpByUserId(String userId) {
        return otpRepository.findByUserId(userId);
    }

    @Override
    public void deleteOtpById(OTP otp) {
        otpRepository.delete(otp);
    }
}
