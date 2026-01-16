package com.okits02.delivery_serivce.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhtkOrderResponse {
    boolean success;
    String message;
    GhtkOrderData order;
}
