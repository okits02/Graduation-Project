package com.example.profile_service.dto.response;

import com.example.profile_service.entity.UserAddress;
import com.example.profile_service.enums.Sex;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileForAdminResponse {
    String userId;
    String avatarUrl;
    String firstName;
    String lastName;
    Sex sex;
    String phone;
    Date dob;
    List<UserAddress> address;
}
