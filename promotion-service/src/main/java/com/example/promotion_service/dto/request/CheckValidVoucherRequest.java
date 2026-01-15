package com.example.promotion_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckValidVoucherRequest {
    String voucherCode;
    Double totalAmount;
    LocalDateTime today;
    List<String> productId;
    List<String> categoryId;
}
