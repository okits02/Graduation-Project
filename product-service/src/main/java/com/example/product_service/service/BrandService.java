package com.example.product_service.service;

import com.example.product_service.dto.request.BrandCreationRequest;
import com.example.product_service.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    public BrandResponse save(BrandCreationRequest request);
    public BrandResponse update(BrandCreationRequest request);
    public List<BrandResponse> getList();
    public void delete(String name);

    public void removeCategoryFromBrand(String categoryId);

}
