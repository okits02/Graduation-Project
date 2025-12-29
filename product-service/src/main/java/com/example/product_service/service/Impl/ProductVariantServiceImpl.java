package com.example.product_service.service.Impl;

import com.example.product_service.dto.request.ProductVariantsRequest;
import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.enums.SpecGroup;
import com.example.product_service.enums.SpecType;
import com.example.product_service.enums.VariantAction;
import com.example.product_service.exceptions.ProductErrorCode;
import com.example.product_service.mapper.ProductVariantsMapper;
import com.example.product_service.model.ProductVariants;
import com.example.product_service.model.Specifications;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.ProductVariantsService;
import com.example.product_service.utils.SkuGenerator;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantsService {
    private final ProductVariantsRepository productVariantsRepository;
    private final ProductVariantsMapper productVariantsMapper;
    @Override
    public List<String> save(List<ProductVariantsRequest> request, String productId) {

        if (request == null || request.isEmpty()) {
            return List.of();
        }

        List<String> responses = request.stream()
                .map(m -> {
                    if (productVariantsRepository.existsByProductIdAndNameAndColor(
                            productId,
                            m.getVariantName(),
                            m.getColor()
                    )) {
                        throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_EXISTS);
                    }
                    ProductVariants productVariants = productVariantsMapper.toProductVariants(m);
                    productVariants.setProductId(productId);
                    productVariants.setSku(SkuGenerator.generateSku());
                    ProductVariants save = productVariantsRepository.save(productVariants);
                    return save.getSku();
                })
                .toList();

        return responses;
    }

    @Override
    public List<String> update(List<ProductVariantsRequest> request, String productId) {
        if (request == null || request.isEmpty()) {
            return List.of();
        }
        List<String> skuResult = new ArrayList<>();

        for(ProductVariantsRequest v : request){
            switch (v.getAction()){
                case CREATE -> {
                    if (productVariantsRepository.existsByProductIdAndNameAndColor(
                            productId,
                            v.getVariantName(),
                            v.getColor()
                    )) {
                        throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_EXISTS);
                    }
                    ProductVariants productVariants = productVariantsMapper.toProductVariants(v);
                    productVariants.setProductId(productId);
                    productVariants.setSku(SkuGenerator.generateSku());
                    ProductVariants save = productVariantsRepository.save(productVariants);
                    skuResult.add(save.getSku());
                }
                case UPDATE -> {
                    if (productVariantsRepository.existsByProductIdAndNameAndColor(
                            productId,
                            v.getVariantName(),
                            v.getColor()
                    )){
                        throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_EXISTS);
                    }
                    ProductVariants variants = productVariantsRepository.findBySku(v.getSku());
                    productVariantsMapper.updateProduct(variants, v);
                    ProductVariants update = productVariantsRepository.save(variants);
                    skuResult.add(update.getSku());
                }
            }
        }
        return skuResult;
    }

    @Override
    public List<ProductVariantsResponse> getListByProductId(String productId) {
        List<ProductVariants> variants = productVariantsRepository.findByProductId(productId);
        if(variants == null || variants.isEmpty()){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        return variants.stream().map(productVariantsMapper::toProductVariantsResponse).toList();
    }

    @Override
    public List<ProductVariants> getVariantForKafkaEvent(String productId) {
        List<ProductVariants> variants = productVariantsRepository.findByProductId(productId);
        if(variants == null || variants.isEmpty()){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        return variants;
    }

    @Override
    public void deleteBySku(String sku) {
        ProductVariants variant = productVariantsRepository.findBySku(sku);
        if(variant == null){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        productVariantsRepository.delete(variant);
    }

    @Override
    public void deleteByProductId(String productId) {
        List<ProductVariants> variants = productVariantsRepository.findByProductId(productId);
        if(variants == null || variants.isEmpty()){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        for (ProductVariants variant : variants){
            productVariantsRepository.delete(variant);
        }
    }


    @Override
    public void changeStock(String sku, Boolean inStock) {
        ProductVariants variant = productVariantsRepository.findBySku(sku);
        if(variant == null){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        variant.setInStock(inStock);
    }
}
