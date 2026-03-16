package com.example.profile_service.entity;

import jakarta.persistence.*;
import jakarta.ws.rs.ext.ParamConverter;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "address")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    UserProfile userProfile;
}
