package com.example.userservice.service;

import com.example.userservice.model.ForgotPassword;
import com.example.userservice.model.Users;
import org.apache.catalina.User;

import java.util.Optional;

public interface ForgotPasswordService {
    ForgotPassword createOTP(Users users, String otp, String sendTo);
    ForgotPassword findById(String id);
    Optional<ForgotPassword> findByUserId(String userId);
    void deleteOTP(String id);
}
