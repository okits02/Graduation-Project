package com.okits02.delivery_serivce.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    String userId;
    String avatarUrl;
    String firstName;
    String lastName;
    String phone;
    Date dob;
    List<AddressResponse> address;
}
