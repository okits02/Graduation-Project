package com.example.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.example.search_service.Repository.ProductsRepository;
import com.example.search_service.exceptions.AppException;
import com.example.search_service.exceptions.ErrorCode;
import com.example.search_service.mapper.ProductsMapper;
import com.example.search_service.model.Products;
import com.example.search_service.model.Promotion;
import com.example.search_service.viewmodel.dto.ApplyPromotionEventDTO;
import com.example.search_service.viewmodel.dto.request.ProductRequest;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;


    public void createProduct(ProductRequest request) {
        productsRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(ErrorCode.PRODUCT_EXISTS));
        Products products = productsMapper.toProducts(request);
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
                .descriptions(request.getDescription())
                .discountPercent(request.getDiscountPercent())
                .fixedAmount(request.getFixedAmount())
                .build();
        if(request.getProductIdList() != null) {
            createPromotionByProductId(promotion, request.getProductIdList());
        }
        if(request.getCategoryIdList() != null) {
            createPromotionByCategoryId(promotion, request.getCategoryIdList());
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

            BulkOperation bulkOperation = BulkOperation.of(b -> b
                    .update(u -> u
                            .index("products")
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

    public void createPromotionByCategoryId(Promotion promotion, Set<String> categoryId) throws IOException {
        List<BulkOperation> bulkOperationList = new LinkedList<>();
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.terms(t -> t.field("categories.id").terms(v -> v.value(
                        categoryId.stream().map(FieldValue::of).toList()
                ))))
                .build();
        SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQuery, Products.class);
        for(SearchHit<Products> hit : searchHits) {
            Products products = hit.getContent();
            if(products.getPromotions() == null){
                products.setPromotions(new HashSet<>());
            }
            products.getPromotions().add(promotion);
            BulkOperation bulkOperation = BulkOperation.of(b -> b
                    .update(u -> u
                            .index("products")
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
}
