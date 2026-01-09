package com.okits02.analys_service.repository;

import com.okits02.analys_service.model.OrderItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrderItemRepository extends ElasticsearchRepository<OrderItem, String> {
}
