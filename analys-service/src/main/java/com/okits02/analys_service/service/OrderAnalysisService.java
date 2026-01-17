package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.okits02.analys_service.dto.OrderAnalysisEvent;
import com.okits02.analys_service.dto.OrderItemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OrderAnalysisService {
    private final ElasticsearchClient elasticsearchClient;

    private void save(OrderAnalysisEvent request) throws IOException {
        if(request == null) return;

        OrderAnalysisEvent orderAnalysisEvent = OrderAnalysisEvent.builder()
                .orderId(request.getOrderId())
                .orderDate(request.getOrderDate())
                .userId(request.getUserId())
                .orderFee(request.getOrderFee())
                .totalPrice(request.getTotalPrice())
                .items(request.getItems().stream().map(i -> OrderItemEvent.builder()
                        .orderId(i.getOrderId())
                        .sku(i.getSku())
                        .quantity(i.getQuantity())
                        .variantName(i.getVariantName())
                        .thumbnailUrl(i.getThumbnailUrl())
                        .listPrice(i.getListPrice())
                        .sellPrice(i.getSellPrice())
                        .addAt(i.getAddAt())
                        .build()).toList())
                .build();

        elasticsearchClient.index(i -> i
                .index("order_analysis")
                .id(request.getId())
                .document(orderAnalysisEvent)
        );
    }
}
