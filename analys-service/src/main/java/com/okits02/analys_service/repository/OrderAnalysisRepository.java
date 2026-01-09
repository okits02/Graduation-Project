package com.okits02.analys_service.repository;

import com.okits02.analys_service.model.OrderAnalysis;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrderAnalysisRepository extends ElasticsearchRepository<OrderAnalysis, String> {
}
