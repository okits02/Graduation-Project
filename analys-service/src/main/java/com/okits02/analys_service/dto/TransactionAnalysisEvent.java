package com.okits02.analys_service.dto;

import com.okits02.analys_service.enums.ReferenceType;
import com.okits02.analys_service.enums.TransactionType;
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
    @Enumerated(EnumType.STRING)
    TransactionType transactionType;
    Integer quantity;
    String referenceId;

    @Enumerated(EnumType.STRING)
    ReferenceType referenceType;
    LocalDateTime createdAt;
}
