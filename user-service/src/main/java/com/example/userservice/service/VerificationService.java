package com.example.userservice.service;

import com.example.userservice.model.OTP;
import com.example.userservice.model.Users;

import java.util.Optional;

public interface VerificationService {
    OTP sendverifyOtp(Users users);
    OTP getOtpById(String otpId);
    Optional<OTP> getOtpByUserId(String userId);
    void deleteOtpById(OTP otp);
}
