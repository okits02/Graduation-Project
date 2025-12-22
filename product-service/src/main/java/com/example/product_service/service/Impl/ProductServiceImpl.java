package com.example.product_service.service.Impl;

import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.enums.SpecType;
import com.example.product_service.model.Product_variants;
import com.example.product_service.model.Specifications;
import com.example.product_service.service.ProductVariantsService;
import com.example.product_service.utils.SkuGenerator;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductMappingHelper productMappingHelper;
    private final ProductVariantsService productVariantsService;

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
        List<ProductVariantsResponse> variantsResponseList = productVariantsService.getListByProductId(productId);
        ProductResponse productResponse = productMappingHelper.map(product);
        productResponse.setVariantsResponses(variantsResponseList);
        return productResponse;
    }

    @Override
    public Products createProduct(ProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ProductErrorCode.PRODUCT_EXISTS);
        }
        Products product = Products.builder()
                .id(new ObjectId().toHexString())
                .name(request.getName())
                .description(request.getDescription())
                .videoUrl(request.getVideoUrl())
                .categoryId(request.getCategoryId())
                .brandName(request.getBrandName())
                .createAt(LocalDate.now())
                .build();
        if (request.getSpecifications() != null) {
            product.setSpecifications(
                    request.getSpecifications().stream()
                            .map(spec -> Specifications.builder()
                                    .key(spec.getKey())
                                    .value(spec.getValue())
                                    .type(SpecType.TECH)
                                    .group(null) // FE sẽ group sau
                                    .build()
                            ).toList()
            );
        }
        if (request.getProduct_variants() != null) {
            List<String> productVariants = productVariantsService.save(request.getProduct_variants(),
                    product.getId());
            product.setVariants(productVariants);
        }
        return productRepository.save(product);
    }

    @Override
    public Products updateProduct(ProductUpdateRequest request) {
        Products products = productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS));
        productMapper.updateProduct(products, request);
        products.setCategoryId(request.getCategoryId());
        List<String> productVariants = productVariantsService.update(request.getProduct_variants(),
                products.getId());
        products.setSpecifications(
                request.getSpecifications().stream()
                        .map(spec -> Specifications.builder()
                                .key(spec.getKey())
                                .value(spec.getValue())
                                .type(SpecType.TECH)
                                .group(null) // FE sẽ group sau
                                .build()
                        ).toList());
        products.setVariants(productVariants);
        productRepository.save(products);
        return products;
    }

    @Override
    public void DeleteProduct(String productId) {
        productRepository.deleteById(productId);
        productVariantsService.deleteByProductId(productId);
    }

    @Override
    public void changeStatusInStock(String sku, Boolean inStock) {
        productVariantsService.changeStock(sku, inStock);
    }

}
