package com.example.profile_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerVM {
    String avatarUrl;
    String firstName;
    String lastName;
}
