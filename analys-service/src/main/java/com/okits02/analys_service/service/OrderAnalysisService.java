package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.okits02.analys_service.viewmodel.dto.OrderAnalysisEvent;
import com.okits02.analys_service.viewmodel.dto.OrderItemEvent;
import com.okits02.analys_service.model.OrderAnalysis;
import com.okits02.analys_service.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OrderAnalysisService {
    private final ElasticsearchClient elasticsearchClient;

    public void save(OrderAnalysisEvent request) throws IOException {
        if (request == null) return;

        // 1️⃣ Index ORDER_ANALYSIS
        OrderAnalysis orderAnalysis = OrderAnalysis.builder()
                .id(request.getOrderId())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .orderStatus(request.getOrderStatus())
                .orderFee(request.getOrderFee())
                .totalPrice(request.getTotalPrice())
                .orderDate(request.getOrderDate().withNano(0))
                .build();

        elasticsearchClient.index(i -> i
                .index("order_analysis")
                .id(orderAnalysis.getId())
                .document(orderAnalysis)
        );

        for (OrderItemEvent item : request.getItems()) {

            OrderItem orderItem = OrderItem.builder()
                    .orderItemId(item.getOrderItemId())
                    .orderId(request.getOrderId())
                    .sku(item.getSku())
                    .variantName(item.getVariantName())
                    .thumbnail(item.getThumbnailUrl())
                    .quantity(item.getQuantity())
                    .listPrice(item.getListPrice())
                    .sellPrice(item.getSellPrice())
                    .addAt(item.getAddAt().withNano(0))
                    .build();

            elasticsearchClient.index(i -> i
                    .index("order_item_analysis")
                    .id(orderItem.getOrderItemId())
                    .document(orderItem)
            );
        }
    }

}
