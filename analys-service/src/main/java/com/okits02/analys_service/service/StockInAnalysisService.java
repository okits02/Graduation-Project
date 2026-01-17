package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.okits02.analys_service.dto.StockInAnalysisEvent;
import com.okits02.analys_service.model.StockInAnalysis;
import com.okits02.analys_service.model.StockInItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StockInAnalysisService {
    private final ElasticsearchClient elasticsearchClient;

    public void create(StockInAnalysisEvent request) throws IOException {
        if(request == null) return;

        StockInAnalysis stockInAnalysis = StockInAnalysis.builder()
                .id(request.getId())
                .createdAt(request.getCreatedAt())
                .totalAmount(request.getTotalAmount())
                .referenceCode(request.getReferenceCode())
                .supplierName(request.getSupplierName())
                .items(request.getItems().stream().map(i -> StockInItem.builder()
                        .sku(i.getSku())
                        .variantName(i.getVariantName())
                        .thumbnail(i.getThumbnail())
                        .totalCost(i.getTotalCost())
                        .unitCost(i.getUnitCost())
                        .quantity(i.getQuantity())
                        .build()).toList())
                .build();

        elasticsearchClient.index(i -> i
                .index("stock_in_analysis")
                .id(request.getId())
                .document(stockInAnalysis)
        );
    }
}
