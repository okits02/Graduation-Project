package com.example.userservice.dto.request;

import com.example.userservice.model.Users;
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
    String city;
    String postal_code;
    String telephone;
}
