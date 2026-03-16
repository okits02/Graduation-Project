package com.example.userservice.kafka;

import com.example.userservice.enums.Sex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProfileEvent {
    private String userId;
    private String firstName;
    private String lastName;
    private Sex sex;
    private String phone;
    private Date dob;
}
