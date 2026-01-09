package com.okits02.analys_service.repository;

import com.okits02.analys_service.model.OrderItem;
import com.okits02.analys_service.model.StockInAnalysis;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface StockInAnalysisRepository extends ElasticsearchRepository<StockInAnalysis, String> {
}
