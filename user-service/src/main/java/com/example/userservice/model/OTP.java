package com.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @OneToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Users user;
    String verification_type;
}
