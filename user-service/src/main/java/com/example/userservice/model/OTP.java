package com.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
public class OTP {
    @Id
    String id;
    String otp_code;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user")
    Users user;
    static final long OTP_VALID_TIME = 5 * 60 * 1000;
    Date otp_request_time;
}
