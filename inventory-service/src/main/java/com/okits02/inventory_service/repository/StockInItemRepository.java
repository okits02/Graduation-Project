package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.StockInItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInItemRepository extends JpaRepository<StockInItem, String> {
}
