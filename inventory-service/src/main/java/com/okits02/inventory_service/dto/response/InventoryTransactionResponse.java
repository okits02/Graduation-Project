package com.okits02.inventory_service.dto.response;

import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionResponse {
    String id;
    String sku;
    String variantName;
    String thumbnail;
    String color;
    TransactionType transactionType;
    Integer quantity;
    String referenceId;
    ReferenceType referenceType;
    String note;
    LocalDateTime createdAt;
}
