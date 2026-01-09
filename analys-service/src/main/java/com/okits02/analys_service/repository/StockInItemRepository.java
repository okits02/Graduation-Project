package com.okits02.analys_service.repository;

import com.okits02.analys_service.model.StockInAnalysis;
import com.okits02.analys_service.model.StockInItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface StockInItemRepository extends ElasticsearchRepository<StockInItem, String> {
}
