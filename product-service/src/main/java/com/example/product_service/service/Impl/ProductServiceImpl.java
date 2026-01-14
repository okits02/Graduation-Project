package com.example.product_service.service.Impl;

import com.example.product_service.enums.SpecType;
import com.example.product_service.kafka.ProductEvent;
import com.example.product_service.kafka.DeleteProductEvent;
import com.example.product_service.model.Specifications;
import com.example.product_service.service.CategoryService;
import com.example.product_service.service.ProductVariantsService;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductMappingHelper productMappingHelper;
    private final ProductVariantsService productVariantsService;
    private final CategoryService categoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        return productMappingHelper.map(product);
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
                .warrantyStartDate(request.getWarrantyStartDate())
                .warrantyEndDate(request.getWarrantyEndDate())
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
                                    .group(spec.getGroup())
                                    .build()
                            ).toList()
            );
        }
        if (request.getProductVariants() != null) {
            List<String> productVariants = productVariantsService.save(request.getProductVariants(),
                    product.getId());
            product.setVariants(productVariants);
        }
        Products response = productRepository.save(product);
        sendKafkaEvent(product, "CREATED");
        return response;
    }

    @Override
    public Products updateProduct(ProductUpdateRequest request) {
        Products product = productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS));
        productMapper.updateProduct(product, request);
        List<String> productVariants = productVariantsService.update(request.getProductVariants(),
                product.getId());
        product.setSpecifications(
                request.getSpecifications().stream()
                        .map(spec -> Specifications.builder()
                                .key(spec.getKey())
                                .value(spec.getValue())
                                .type(SpecType.TECH)
                                .group(spec.getGroup())
                                .build()
                        ).toList());
        product.setVariants(productVariants);
        productRepository.save(product);
        sendKafkaEvent(product, "UPDATED");
        return product;
    }

    @Override
    public void DeleteProduct(String productId) {
        Optional<Products> product = productRepository.findById(productId);
        if(product.get() == null ){
            throw new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS);
        }
        sendKafkaEvent(product.get(), "DELETED");
        productRepository.delete(product.get());
        productVariantsService.deleteByProductId(productId);
    }

    @Override
    public void DeleteListProduct(List<String> productIds) {
        sendDeleteProductEvent(productIds, "DELETE_LIST");
        for(String id : productIds){
            Optional<Products> product = productRepository.findById(id);
            if(product.get() == null ){
                throw new AppException(ProductErrorCode.PRODUCT_NOT_EXISTS);
            }
            productRepository.delete(product.get());
            productVariantsService.deleteByProductId(id);
        }
    }

    @Override
    public void DeleteAll() {
        sendDeleteProductEvent(List.of(), "DELETE_ALL");
        productRepository.deleteAll();
    }


    @Override
    public void changeStatusInStock(String sku, Boolean inStock) {
        productVariantsService.changeStock(sku, inStock);
    }

    private void sendKafkaEvent(Products product, String typeEvent){
        if(typeEvent == null){
            return;
        }
        switch (typeEvent){
            case "CREATED" -> {
                ProductEvent productEvent = createEventProduct(product, typeEvent);
                kafkaTemplate.send("product-event", productEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
            case "UPDATED" -> {
                ProductEvent productEvent = createEventProduct(product, typeEvent);
                kafkaTemplate.send("product-event", productEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
            case "DELETED" -> {
                ProductEvent productEvent = createEventProduct(product, typeEvent);
                kafkaTemplate.send("product-event", productEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
        }
    }

    private void sendDeleteProductEvent(List<String> productIds, String deleteEventType){
        switch (deleteEventType){
            case "DELETE_LIST" -> {
                DeleteProductEvent deleteProductEvent = DeleteProductEvent.builder()
                        .deleteEventType("DELETE_LIST")
                        .productId(productIds)
                        .build();
                kafkaTemplate.send("product-delete-topics", deleteProductEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
            case "DELETE_ALL" -> {
                DeleteProductEvent deleteProductEvent = DeleteProductEvent.builder()
                        .deleteEventType("DELETE_ALL")
                        .build();
                kafkaTemplate.send("product-delete-topics", deleteProductEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else
                            {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
        }
    }
    private ProductEvent createEventProduct(Products product, String eventType)
    {
        List<String> categoryList = new ArrayList<>();
        Set<String> currentCateId =
                Optional.ofNullable(product.getCategoryId())
                        .orElse(Set.of());

        if (!currentCateId.isEmpty()) {
            categoryList = categoryService.getCategoryHierarchy(currentCateId);
        }
        ProductEvent productEvent = ProductEvent.builder()
                .eventType(eventType)
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrandName())
                .description(product.getDescription())
                .videoUrl(product.getVideoUrl())
                .categoriesId(categoryList)
                .specifications(product.getSpecifications())
                .warrantyStartDate(product.getWarrantyStartDate())
                .warrantyEndDate(product.getWarrantyEndDate())
                .productVariants(productVariantsService.getVariantForKafkaEvent(product.getId()))
                .createAt(product.getCreateAt())
                .updateAt(product.getUpdateAt())
                .build();
        return productEvent;
    }
}
