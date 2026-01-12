package com.example.profile_service.dto.request;

import com.example.profile_service.enums.Sex;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileRequest {
    String userId;
    Sex sex;
    String firstName;
    String lastName;
    String phone;
    Date dob;
}
