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
@Table( name = "users",
        indexes = {
            @Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_email", columnList = "email")
        })
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(nullable = false, unique = true)
    String username;

    @Column(nullable = false, unique = true)
    String email;
    String password;
    Boolean isActive;
    Boolean isVerified;
    @ManyToOne
    @JoinColumn(name = "role_name", referencedColumnName = "name")
    Role role;
}
