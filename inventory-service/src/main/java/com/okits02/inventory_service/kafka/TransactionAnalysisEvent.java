package com.okits02.inventory_service.kafka;

import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionAnalysisEvent {
    String id;
    String sku;
    String variantName;
    String thumbnail;
    @Enumerated(EnumType.STRING)
    TransactionType transactionType;
    Integer quantity;
    String referenceId;

    @Enumerated(EnumType.STRING)
    ReferenceType referenceType;
    LocalDateTime createdAt;
}
