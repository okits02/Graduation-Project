package com.example.userservice.dto.response;

import com.example.userservice.enums.Sex;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse {
    String userId;
    String avatarUrl;
    String firstName;
    String lastName;
    Sex sex;
    String phone;
    Date dob;
}
