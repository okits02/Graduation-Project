package com.example.userservice.dto.response;

import com.example.userservice.model.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String username;
    String email;
    String avatarUrl;
    String firstName;
    String lastName;
    String phone;
    Date dob;
    Role role;
    List<UserAddressResponse> address;
}
