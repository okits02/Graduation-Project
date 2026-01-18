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

    String  transactionType;
    Integer quantity;
    String referenceId;

    String referenceType;
    LocalDateTime createdAt;
}
