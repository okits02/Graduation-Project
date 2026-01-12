package com.example.search_service.viewmodel;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteProductEvent {
    String deleteEventType;
    List<String> productId;
}
