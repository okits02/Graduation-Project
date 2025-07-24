package com.example.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.example.search_service.Repository.ProductsRepository;
import com.example.search_service.exceptions.AppException;
import com.example.search_service.exceptions.ErrorCode;
import com.example.search_service.mapper.ProductsMapper;
import com.example.search_service.mapper.PromotionMapper;
import com.example.search_service.model.Products;
import com.example.search_service.model.Promotion;
import com.example.search_service.viewmodel.dto.ApplyPromotionEventDTO;
import com.example.search_service.viewmodel.dto.StatusPromotionDTO;
import com.example.search_service.viewmodel.dto.request.ProductRequest;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.annotation.Native;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;
    private final PromotionMapper promotionMapper;


    public void createProduct(ProductRequest request) {
        if(productsRepository.existsById(request.getId())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTS);
        }
        Products products = productsMapper.toProducts(request);
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                        .bool(b -> b
                                .must(must -> must
                                        .nested(nested -> nested
                                                .path("categories")
                                                .query(q -> q
                                                        .bool(bool -> bool
                                                                .must(m1 -> m1
                                                                        .terms(t -> t
                                                                                .field("categories.name")
                                                                                .terms(terms -> terms
                                                                                        .value(products.getCategories()
                                                                                                .stream()
                                                                                                .map(FieldValue::of)
                                                                                                .toList()
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .must(must1 -> must1
                                        .nested(nested1 -> nested1
                                                .path("promotions")
                                                .query(q1 -> q1
                                                        .bool(b1 -> b1
                                                                .must(m1 -> m1
                                                                        .term(t1 -> t1
                                                                                .field("promotions.applyTo")
                                                                                .value("Category")
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();
        SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQuery, Products.class);
        Set<Promotion> newPromotion = new HashSet<>();
        if(!searchHits.isEmpty()) {
            for(SearchHit<Products> hit : searchHits) {
                Products products1 =  hit.getContent();
                for(Promotion pro : products1.getPromotions()) {
                    if(pro.getApplyTo() != null)
                    {
                        if(pro.getApplyTo().equals("Category") && pro.getActive().equals(Boolean.TRUE)) {
                            newPromotion.add(pro);
                        }
                    }
                }
                break;
            }
        }
        products.setPromotions(newPromotion);
        products.calculatorSellPrice();
        productsRepository.save(products);
    }

    public void updateProduct(ProductRequest request) {
        Products products = productsRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(ErrorCode.PRODUCT_NOT_EXISTS));
        productsMapper.updateProduct(products, request);
        productsRepository.save(products);
    }

    public void deleteProduct(String productId) {
        productsRepository.deleteById(productId);
    }

    public void createPromotion(ApplyPromotionEventDTO request) throws IOException {
        Promotion promotion = Promotion.builder()
                .id(request.getId())
                .name(request.getName())
                .descriptions(request.getDescriptions())
                .applyTo(request.getApplyTo())
                .discountPercent(request.getDiscountPercent())
                .fixedAmount(request.getFixedAmount())
                .active(request.getActive())
                .createAt(request.getCreateAt())
                .updateAt(request.getUpdateAt())
                .build();
        if(request.getProductIdList() != null) {
            createPromotionByProductId(promotion, request.getProductIdList());
        }
        if(request.getCategoryNameList() != null) {
            createPromotionByCategoryId(promotion, request.getCategoryNameList());
        }
        log.info("create promotion successfully");
    }

    public void updatePromotion(ApplyPromotionEventDTO request) {
        List<BulkOperation> operations = new ArrayList<>();
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .nested(nes -> nes
                                                .path("promotions")
                                                .query(query -> query.term(t -> t
                                                        .field("promotions.id")
                                                        .value(request.getId())
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();
        SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQuery, Products.class);
        for(SearchHit<Products> hit : searchHits) {
            Products product = hit.getContent();
            if(product.getPromotions() != null) {
                for (Promotion pro : product.getPromotions()) {
                    if(pro.getId().equals(request.getId())) {
                        promotionMapper.updatePromotion(pro, request);
                    }
                }
                product.calculatorSellPrice();
                BulkOperation bulkOperation = BulkOperation.of(b -> b
                        .update(u -> u
                                .index("product")
                                .id(product.getId())
                                .action(a -> a.doc(product)
                                )
                        )
                );
            }
        }
    }

    public void createPromotionByProductId(Promotion promotion, Set<String> listProductId) throws IOException {
        List<BulkOperation> operations = new ArrayList<>();
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.terms(t -> t.field("id").terms(v -> v.value(
                        listProductId.stream().map(FieldValue::of).toList()
                ))))
                .build();

        SearchHits<Products> searchHits = elasticsearchOperations.search(searchQuery, Products.class);


        for (SearchHit<Products> hit : searchHits) {
            Products product = hit.getContent();

            if (product.getPromotions() == null) {
                product.setPromotions(new HashSet<>());
            }
            product.getPromotions().add(promotion);
            product.calculatorSellPrice();
            BulkOperation bulkOperation = BulkOperation.of(b -> b
                    .update(u -> u
                            .index("product")
                            .id(product.getId())
                            .action(a -> a
                                    .doc(product)
                            )
                    )
            );

            operations.add(bulkOperation);
        }

        if (!operations.isEmpty()) {
            BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(operations));

            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

            if (bulkResponse.errors()) {
                throw new IOException("Bulk update promotions failed: " + bulkResponse.toString());
            } else {
                System.out.println("Updated promotions for " + operations.size() + " products.");
            }
        } else {
            System.out.println("No products found for given product IDs.");
        }
    }

    public void createPromotionByCategoryId(Promotion promotion, Set<String> listCategoryName) throws IOException {
        List<BulkOperation> bulkOperationList = new LinkedList<>();
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                        .bool(b -> b
                                .must(must -> must
                                        .nested(nested -> nested
                                                .path("categories")
                                                .query(q -> q
                                                        .bool(bool -> bool
                                                                .must(m1 -> m1
                                                                        .terms(t -> t
                                                                                .field("categories.name")
                                                                                .terms(terms -> terms
                                                                                        .value(listCategoryName.stream()
                                                                                                .map(FieldValue::of)
                                                                                                .toList()
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();
        SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQuery, Products.class);
        for(SearchHit<Products> hit : searchHits) {
            Products products = hit.getContent();
            if(products.getPromotions() == null){
                products.setPromotions(new HashSet<>());
            }
            products.getPromotions().add(promotion);
            products.calculatorSellPrice();
            BulkOperation bulkOperation = BulkOperation.of(b -> b
                    .update(u -> u
                            .index("product")
                            .id(products.getId())
                            .action(a -> a.doc(products))));
            bulkOperationList.add(bulkOperation);
        }
        if (!bulkOperationList.isEmpty()) {
            BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(bulkOperationList));

            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);
            if (bulkResponse.errors()) {
                throw new IOException("Bulk update promotions failed: " + bulkResponse.toString());
            } else {
                System.out.println("Updated promotions for " + bulkOperationList.size() + " products.");
            }
        } else {
            System.out.println("No products found for given product IDs.");
        }
    }


    public void updateStatusPromotion(StatusPromotionDTO request) throws IOException {
        List<BulkOperation> operations = new LinkedList<>();
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("promotion.id")
                                .value(request.getId())))
                .build();
        SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQuery, Products.class);
        for(SearchHit<Products> hit : searchHits){
            Products products = hit.getContent();
            if(products.getPromotions() != null) {
                products.getPromotions().forEach(promotion -> {
                    if(promotion.getId().equals(request.getId())){
                        promotion.setActive(false);
                    }
                });
            }
            BulkOperation bulkOperation = BulkOperation.of(b -> b
                    .update(u -> u
                            .index("products")
                            .id(products.getId())
                            .action(a -> a.doc(products))));
            operations.add(bulkOperation);
        }
        if (!operations.isEmpty()) {
            BulkRequest bulkRequest = new BulkRequest.Builder()
                    .operations(operations)
                    .build();
            elasticsearchClient.bulk(bulkRequest);
        }
    }
}
