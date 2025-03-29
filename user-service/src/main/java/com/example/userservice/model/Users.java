package com.example.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String password;
    String firstName;
    String lastName;
    String email;
    String phone;
    boolean isActive;
    boolean isVerified;
    @OneToMany(mappedBy = "user")
    List<OTP> otps;
    @ManyToOne
    @JoinColumn(name = "role_name", referencedColumnName = "name")
    Role role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<UserAddress> addressList = new ArrayList<>();
}
