package com.example.product_service.service.Impl;

import com.example.product_service.dto.request.ProductVariantsRequest;
import com.example.product_service.dto.request.SpecificationRequest;
import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.exceptions.ProductErrorCode;
import com.example.product_service.mapper.ProductVariantsMapper;
import com.example.product_service.model.Product_variants;
import com.example.product_service.model.Specifications;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.ProductVariantsService;
import com.example.product_service.utils.SkuGenerator;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                            m.getVariants_name(),
                            m.getColor()
                    )) {
                        throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_EXISTS);
                    }

                    Product_variants productVariants = productVariantsMapper.toProductVariants(m);
                    productVariants.setProductId(productId);
                    productVariants.setSku(SkuGenerator.generateSku());

                    Product_variants save = productVariantsRepository.save(productVariants);
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

        List<String> responses = request.stream()
                .map(m -> {
                    Product_variants variant = productVariantsRepository.findBySku(m.getSku());
                    if(variant == null){
                        throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
                    }
                    if (!variant.getProductId().equals(productId)) {
                        throw new AppException(ProductErrorCode.INVALID_PRODUCT_VARIANT);
                    }
                    if(m.getColor() != null){
                        String oldColor = variant.getBestSpecifications().stream()
                                .filter(s -> "color".equalsIgnoreCase(s.getKey()))
                                .map(Specifications::getValue)
                                .findFirst()
                                .orElse(null);
                        if(!m.getColor().equals(oldColor)){
                            if (productVariantsRepository.existsByProductIdAndNameAndColor(
                                    productId,
                                    m.getVariants_name(),
                                    m.getColor()
                            )) {
                                throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_EXISTS);
                            }
                            List<Specifications> specs = variant.getBestSpecifications();
                            Specifications colorSpec = specs.stream()
                                    .filter(s -> "color".equalsIgnoreCase(s.getKey()))
                                    .findFirst()
                                    .orElse(null);
                            if(colorSpec != null){
                                colorSpec.setValue(m.getColor());
                            }else {
                                specs.add(Specifications.builder()
                                        .key("color")
                                        .value(m.getColor())
                                        .build());
                            }
                        }
                    }
                    productVariantsMapper.updateProduct(variant, m);
                    Product_variants save = productVariantsRepository.save(variant);
                    return save.getSku();
                })
                .toList();

        return responses;
    }

    @Override
    public List<ProductVariantsResponse> getListByProductId(String productId) {
        List<Product_variants> variants = productVariantsRepository.findByProductId(productId);
        if(variants == null || variants.isEmpty()){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        return variants.stream().map(productVariantsMapper::toProductVariantsResponse).toList();
    }

    @Override
    public void deleteBySku(String sku) {
        Product_variants variant = productVariantsRepository.findBySku(sku);
        if(variant == null){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        productVariantsRepository.delete(variant);
    }

    @Override
    public void deleteByProductId(String productId) {
        List<Product_variants> variants = productVariantsRepository.findByProductId(productId);
        if(variants == null || variants.isEmpty()){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        for (Product_variants variant : variants){
            productVariantsRepository.delete(variant);
        }
    }


    @Override
    public void changeStock(String sku, Boolean inStock) {
        Product_variants variant = productVariantsRepository.findBySku(sku);
        if(variant == null){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        variant.setInStock(inStock);
    }
}
