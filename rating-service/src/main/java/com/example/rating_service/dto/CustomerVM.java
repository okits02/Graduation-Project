package com.example.rating_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerVM {
    String avatarUrl;
    String firstName;
    String lastName;
}
