package com.okits02.inventory_service.service.Impl;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.inventory_service.dto.request.StockInCreationRequest;
import com.okits02.inventory_service.dto.request.StockInItemRequest;
import com.okits02.inventory_service.dto.response.StockInItemResponse;
import com.okits02.inventory_service.dto.response.StockInResponse;
import com.okits02.inventory_service.exceptions.StockInErrorCode;
import com.okits02.inventory_service.mapper.StockInItemMapper;
import com.okits02.inventory_service.mapper.StockInMapper;
import com.okits02.inventory_service.model.StockIn;
import com.okits02.inventory_service.model.StockInItem;
import com.okits02.inventory_service.repository.StockInRepository;
import com.okits02.inventory_service.service.InventoryService;
import com.okits02.inventory_service.service.StockInService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockInServiceImpl implements StockInService {
    private final StockInRepository stockInRepository;
    private final InventoryService inventoryService;
    private final StockInMapper stockInMapper;
    private final StockInItemMapper stockInItemMapper;


    @Override
    public StockInResponse save(StockInCreationRequest request) {
        if(stockInRepository.existsByReferenceCode(request.getReferenceCode())){
            throw new AppException(StockInErrorCode.STOCK_IN_EXISTS_BY_REFERENCE_CODE);
        }
        StockIn newStockIn = stockInRepository.save(stockInMapper.toStockIn(request));
        List<StockInItem> items = createItemStockIn(newStockIn, request.getItems());
        newStockIn.setItems(items);
        List<StockInItemResponse> itemsResponse = new ArrayList<>();
        stockInRepository.save(newStockIn);
        inventoryService.save(request.getItems(), newStockIn.getId());
        return StockInResponse.builder()
                .referenceCode(newStockIn.getReferenceCode())
                .supplierName(newStockIn.getSupplierName())
                .totalAmount(newStockIn.getTotalAmount())
                .createAt(newStockIn.getCreatedAt())
                .items(newStockIn.getItems()
                        .stream()
                        .map(stockInItemMapper::toResponse)
                        .toList())
                .note(newStockIn.getNote())
                .build();
    }

    @Override
    public StockInResponse getByReferenceCode(String referenceCode) {
        Optional<StockIn> stockIn = stockInRepository.findByReferenceCode(referenceCode);
        if(stockIn.get() == null){
            throw new AppException(StockInErrorCode.STOCK_IN_NOT_EXISTS);
        }
        List<StockInItemResponse> itemsResponse = new ArrayList<>();
        for(StockInItem item : stockIn.get().getItems()){
            itemsResponse.add(stockInItemMapper.toResponse(item));
        }
        return StockInResponse.builder()
                .referenceCode(stockIn.get().getReferenceCode())
                .supplierName(stockIn.get().getSupplierName())
                .totalAmount(stockIn.get().getTotalAmount())
                .createAt(stockIn.get().getCreatedAt())
                .items(itemsResponse)
                .note(stockIn.get().getNote())
                .build();
    }

    @Override
    public PageResponse<StockInResponse> getAllHistory(int page, int size, LocalDateTime start, LocalDateTime end) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = stockInRepository.getAllHistory(start, end, pageable);
        List<StockInResponse> stockInResponses = pageData.getContent()
                .stream()
                .map(stockIn -> StockInResponse.builder()
                        .supplierName(stockIn.getSupplierName())
                        .referenceCode(stockIn.getReferenceCode())
                        .note(stockIn.getNote())
                        .totalAmount(stockIn.getTotalAmount())
                        .items(stockIn.getItems().stream().map(item -> StockInItemResponse.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .unitCost(item.getUnitCost())
                                .totalCost(item.getTotalCost())
                                .build()).toList())
                        .createAt(stockIn.getCreatedAt())
                        .build()).toList();
        return PageResponse.<StockInResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(stockInResponses)
                .build();
    }

    @Override
    public void deleteStockIn(String referenceCode) {
        Optional<StockIn> stockIn = stockInRepository.findByReferenceCode(referenceCode);
        if(stockIn.get() == null){
            throw new AppException(StockInErrorCode.STOCK_IN_NOT_EXISTS);
        }
        stockInRepository.delete(stockIn.get());
    }

    private List<StockInItem> createItemStockIn(StockIn stockIn, List<StockInItemRequest> request){
        List<StockInItem> items = new ArrayList<>();
        for(StockInItemRequest item : request){
            StockInItem stockInItem = StockInItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .unitCost(item.getUnitCost())
                    .stockIn(stockIn)
                    .build();
            items.add(stockInItem);
        }
        return items;
    }
}
