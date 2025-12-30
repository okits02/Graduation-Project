package com.example.order_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckValidVoucherRequest {
    String voucherCode;
    LocalDate orderDate;
    BigDecimal totalPrice;
    List<String> productId;
}
