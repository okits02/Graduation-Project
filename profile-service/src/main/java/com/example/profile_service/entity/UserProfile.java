package com.example.profile_service.entity;

import com.example.profile_service.enums.Sex;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "profile")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(nullable = false)
    String userId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Sex sex;
    String avatarUrl;
    String firstName;
    String lastName;
    String phone;
    Date dob;
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    List<UserAddress> addresses;
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductWarranty> warranties;
}
