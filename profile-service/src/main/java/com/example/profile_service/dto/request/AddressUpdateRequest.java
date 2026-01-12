package com.example.profile_service.dto.request;


import com.example.profile_service.validator.UserPhoneConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressUpdateRequest {
    String id;
    String receiverName;
    @UserPhoneConstraint(min = 10, message = "INVALID_PHONE")
    String receiverPhone;
    String addressLine;
    String street;
    String ward;
    String district;
    String city;
    String postalCode;
    String addressType;
}
