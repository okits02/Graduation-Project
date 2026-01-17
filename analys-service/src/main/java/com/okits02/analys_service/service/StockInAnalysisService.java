package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.okits02.analys_service.viewmodel.dto.StockInAnalysisEvent;
import com.okits02.analys_service.model.StockInAnalysis;
import com.okits02.analys_service.model.StockInItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockInAnalysisService {
    private final ElasticsearchClient elasticsearchClient;

    public void create(StockInAnalysisEvent request) throws IOException {
        if (request == null || request.getItems() == null) return;
        List<StockInItem> items = request.getItems().stream()
                .map(i -> {
                    BigDecimal totalCost =
                            i.getUnitCost()
                                    .multiply(BigDecimal.valueOf(i.getQuantity()));

                    return StockInItem.builder()
                            .id(i.getId())
                            .stockInId(request.getId())
                            .sku(i.getSku())
                            .variantName(i.getVariantName())
                            .thumbnail(i.getThumbnail())
                            .quantity(i.getQuantity())
                            .unitCost(i.getUnitCost())
                            .totalCost(totalCost)
                            .build();
                })
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(StockInItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StockInAnalysis stockInAnalysis = StockInAnalysis.builder()
                .id(request.getId())
                .supplierName(request.getSupplierName())
                .referenceCode(request.getReferenceCode())
                .createdAt(request.getCreatedAt())
                .totalAmount(totalAmount)
                .items(items)
                .build();

        elasticsearchClient.index(i -> i
                .index("stock_in_analysis")
                .id(stockInAnalysis.getId())
                .document(stockInAnalysis)
        );
        for (StockInItem item : items) {
            elasticsearchClient.index(i -> i
                    .index("stock_in_item_analysis")
                    .id(item.getId())
                    .document(item)
            );
        }
    }
    public void delete(String stockInId) throws IOException {
        if (stockInId == null) return;
        elasticsearchClient.delete(d -> d
                .index("stock_in_analysis")
                .id(stockInId)
        );
        elasticsearchClient.deleteByQuery(d -> d
                .index("stock_in_item_analysis")
                .query(q -> q
                        .term(t -> t
                                .field("stockInId")
                                .value(stockInId)
                        )
                )
        );
    }
}
