package com.example.profile_service.entity;

import jakarta.persistence.GenerationType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("user_profile")
public class UserProfile {
    @Id
    @GeneratedValue
    String id;
    @Property("userId")
    String userId;
    String firstName;
    String lastName;
    String email;
    String phone;
    Date dob;

    @Relationship(type = "HAS_ADDRESS", direction = Relationship.Direction.OUTGOING)
    List<UserAddress> address;
}
