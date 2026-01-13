package com.example.userservice.dto.response;

import com.example.userservice.enums.Sex;
import com.example.userservice.model.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoResponse {
    String username;
    String firstName;
    String lastName;
    Sex sex;
    String email;
    String phone;
    String dob;
    Role role;
    List<UserAddressResponse> address;
    Boolean isActive;
    Boolean isVerified;
}
