package com.example.userservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntrospectResponse {
    boolean valid;
    boolean verified;
}
