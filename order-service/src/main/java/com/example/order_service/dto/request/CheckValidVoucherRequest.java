package com.example.order_service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckValidVoucherRequest {
    String voucherCode;
    Double totalAmount;
    LocalDateTime today;
    List<String> productId;
    List<String> categoryId;
}
