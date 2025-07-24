package com.example.userservice.dto.response;

import com.example.userservice.model.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String username;
    String firstName;
    String lastName;
    String email;
    String phone;
    Role role;
    Date dob;
}
