package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.okits02.analys_service.dto.TransactionAnalysisEvent;
import com.okits02.analys_service.model.InventoryTransactionAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {
    private final ElasticsearchClient elasticsearchClient;

    public void create(TransactionAnalysisEvent request) throws IOException {
        if (request == null) return;

        InventoryTransactionAnalysis inventoryTransactionAnalysis = InventoryTransactionAnalysis.builder()
                .id(request.getId())
                .referenceId(request.getReferenceId())
                .quantity(request.getQuantity())
                .sku(request.getSku())
                .variantName(request.getVariantName())
                .thumbnail(request.getThumbnail())
                .referenceType(request.getReferenceType())
                .createdAt(request.getCreatedAt())
                .build();

        elasticsearchClient.index(i -> i
                .index("inventory_transaction_analysis")
                .id(request.getId())
                .document(inventoryTransactionAnalysis)
        );
    }
}
