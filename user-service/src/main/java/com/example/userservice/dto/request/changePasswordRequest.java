package com.example.userservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class changePasswordRequest {
    @Size(min = 8, message = "USER_PASSWORD_INVALID")
    String oldPassword;
    @Size(min = 8, message = "USER_PASSWORD_INVALID")
    String newPassword;
}
