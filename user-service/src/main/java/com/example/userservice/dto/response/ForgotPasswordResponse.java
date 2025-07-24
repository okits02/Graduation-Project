package com.example.userservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordResponse {
    String email;
    String token;
    boolean authenticated;
}
