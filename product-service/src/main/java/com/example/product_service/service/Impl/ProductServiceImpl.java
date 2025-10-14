package com.example.product_service.service.Impl;

import com.okits02.common_lib.dto.PageResponse;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.product_service.exceptions.ProductErrorCode;
import com.example.product_service.helper.ProductMappingHelper;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.model.Products;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductMappingHelper productMappingHelper;

    @Override
    public PageResponse<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = productRepository.findAll(pageable);
        return PageResponse.<ProductResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(productMappingHelper::map).toList())
                .build();
    }

    @Override
    public ProductResponse getById(String productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS));
        ProductResponse productResponse = productMappingHelper.map(product);
        return productResponse;
    }

    @Override
    public Products createProduct(ProductRequest request) {
        Products newProducts = productMapper.toProduct(request);
        if(productRepository.existsByName(request.getName())) {
            throw new AppException(ProductErrorCode.PRODUCT_EXISTS);
        }
        String generatedId = new ObjectId().toHexString();
        newProducts.setId(generatedId);
        newProducts.setInStock(false);
        newProducts.setCreateAt(LocalDate.now());
        return productRepository.save(newProducts);
    }

    @Override
    public Products updateProduct(ProductUpdateRequest request) {
        Products products = productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS));
        productMapper.updateProduct(products, request);
        products.setCategoryId(request.getCategoryId());
        productRepository.save(products);
        return products;
    }

    @Override
    public void DeleteProduct(String productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public void changeStatusInStock(String productId, Boolean inStock) {
        Products products = productRepository.findById(productId).orElseThrow(()->
                new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS));
        productRepository.updateStockById(productId, inStock);
    }

}
