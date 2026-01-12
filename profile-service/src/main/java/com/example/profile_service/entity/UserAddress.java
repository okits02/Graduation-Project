package com.example.profile_service.entity;

import com.example.profile_service.validator.UserPhoneConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("user_address")
public class UserAddress {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
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
