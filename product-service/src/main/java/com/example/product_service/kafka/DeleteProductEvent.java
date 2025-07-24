package com.example.product_service.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteProductEvent {
    String productId;
}
