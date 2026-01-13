package com.example.notification_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerVM {
    String avatarUrl;
    String firstName;
    String lastName;
}
