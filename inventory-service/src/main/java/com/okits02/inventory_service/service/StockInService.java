package com.okits02.inventory_service.service;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.request.StockInCreationRequest;
import com.okits02.inventory_service.dto.response.StockInResponse;

import java.time.LocalDateTime;

public interface StockInService {
    public StockInResponse save(StockInCreationRequest request);
    public StockInResponse getByReferenceCode(String referenceCode);
    public PageResponse<StockInResponse> getAllHistory(int page, int size, LocalDateTime start, LocalDateTime end);
    public void deleteStockIn(String referenceCode);
    public StockInResponse getById(String stockInId);
}
