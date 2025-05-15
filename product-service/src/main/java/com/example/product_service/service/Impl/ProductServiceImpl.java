package com.example.product_service.service.Impl;

import com.example.product_service.dto.PageResponse;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.exceptions.ErrorCode;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.model.Products;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    @Override
    public PageResponse<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = productRepository.findAll(pageable);
        return PageResponse.<ProductResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(productMapper::toProductResponse).toList())
                .build();
    }

    @Override
    public ProductResponse getById(String productId) {
        Optional<Products> products = productRepository.findById(productId);
        if(products.isEmpty())
        {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        return productMapper.toProductResponse(products);
    }

    @Override
    public Products createProduct(ProductRequest request) {
        productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ErrorCode.PRODUCT_NOT_EXISTS));
        Products products = productMapper.toProduct(request);
        productRepository.save(products);
        return products;
    }

    @Override
    public ProductResponse updateProduct(ProductRequest request) {
        Products products = productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ErrorCode.PRODUCT_NOT_EXISTS));
         productMapper.updateProduct(products, request);
         return productMapper.toProductResponse(productRepository.save(products));
    }

    @Override
    public void DeleteProduct(String productId) {
        productRepository.deleteById(productId);
    }
}
