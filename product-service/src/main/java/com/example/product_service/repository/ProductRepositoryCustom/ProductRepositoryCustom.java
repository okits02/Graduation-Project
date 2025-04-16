package com.example.product_service.repository.ProductRepositoryCustom;

import com.example.product_service.dto.request.ProductSearchRequest;
import com.example.product_service.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
public interface ProductRepositoryCustom {
    public Page<ProductResponse> searchByCriteria(ProductSearchRequest request);
}
