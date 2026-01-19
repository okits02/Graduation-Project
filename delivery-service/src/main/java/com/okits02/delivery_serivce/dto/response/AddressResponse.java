package com.okits02.delivery_serivce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    String id;
    String receiverName;
    String receiverPhone;
    String addressLine;
    String street;
    String ward;
    String district;
    String city;
    String postalCode;
    String addressType;
}
