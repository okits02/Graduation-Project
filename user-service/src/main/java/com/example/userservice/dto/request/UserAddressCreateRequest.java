package com.example.userservice.dto.request;

import com.example.userservice.model.Users;
import com.example.userservice.validator.PostalCodeConstraint;
import com.example.userservice.validator.UserPhoneConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddressCreateRequest {
    Users user;
    String address_line1;
    String address_line2;
    @NotBlank(message = "City cannot be blank")
    @Size(max = 100, message = "City must not exceed 100 characters")
    String city;
    @PostalCodeConstraint(message = "INVALID_POSTAL_CODE")
    String postal_code;
    @UserPhoneConstraint(min = 10, message = "INVALID_PHONE")
    String telephone;
}
