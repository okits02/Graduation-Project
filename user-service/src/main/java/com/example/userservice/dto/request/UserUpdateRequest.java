package com.example.userservice.dto.request;

import com.example.userservice.model.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String Id;
    String username;
    String password;
    String firstName;
    String lastName;
    String email;
    String phone;
    Role role;
}
