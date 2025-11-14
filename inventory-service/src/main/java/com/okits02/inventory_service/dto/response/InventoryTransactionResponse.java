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
    private String id;
    private String productId;
    private TransactionType transactionType;
    private Integer quantity;
    private String referenceId;
    private ReferenceType referenceType;
    private String note;
    private LocalDateTime createdAt;
}
