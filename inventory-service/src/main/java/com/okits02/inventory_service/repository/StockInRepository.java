package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInRepository extends JpaRepository<StockIn, String> {
}
