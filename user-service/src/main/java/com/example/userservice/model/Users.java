package com.example.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
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
    @ManyToOne
    @JoinColumn(name = "role_name", referencedColumnName = "name")
    Role role;
}
