package com.okits02.analys_service.repository;

import com.okits02.analys_service.model.InventoryTransactionAnalysis;
import com.okits02.analys_service.model.StockInItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface InventoryTransactionAnalysisRepository extends
        ElasticsearchRepository<InventoryTransactionAnalysis, String> {
}
