package com.example.userservice.dto.request;

import com.example.userservice.validator.EmailConstraint;
import com.example.userservice.validator.UserPhoneConstraint;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String Id;
    @Size(min = 3, message = "USERNAME_INVALID")
    String username;
    @Size(min = 8, message = "USER_PASSWORD_INVALID")
    String password;
    String firstName;
    String lastName;
    @EmailConstraint(message = "INVALID_EMAIL")
    String email;
    @UserPhoneConstraint(min = 10, message = "INVALID_PHONE")
    String phone;
}
