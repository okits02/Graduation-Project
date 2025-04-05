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
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String otp_code;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    Users user;
    Date otp_request_time;
}
