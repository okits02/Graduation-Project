package com.example.profile_service.dto.request;

import com.example.profile_service.enums.Sex;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {
    String avatarUrl;
    String firstName;
    String lastName;
    Sex sex;
    String phone;
    Date dob;
}
