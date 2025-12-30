package com.example.order_service.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionResponse {
    String id;
    String name;
    String voucherCode;
    String descriptions;
    Double discountPercent;
    Double fixedAmount;
    Date startDate;
    Date endDate;
    Double minimumOrderPurchaseAmount;
    Double maxDiscountAmount;
    Boolean active;
    List<String> productId;
    List<String> categoryId;
    LocalDate createAt;
    LocalDate updateAt;
}
