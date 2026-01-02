package com.example.search_service.viewmodel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoCompletedResponse {
    String autoCompletedType;
    String id;
    String value;
}
