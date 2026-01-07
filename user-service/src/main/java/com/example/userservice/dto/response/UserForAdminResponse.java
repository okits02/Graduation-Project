package com.example.userservice.dto.response;

import com.example.userservice.model.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserForAdminResponse {
    String id;
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
