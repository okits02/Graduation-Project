package com.example.promotion_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckValidVoucher {
    String voucherCode;
    BigDecimal totalAmount;
    LocalDate today;
    List<String> productId;
    List<String> categoryId;
}
