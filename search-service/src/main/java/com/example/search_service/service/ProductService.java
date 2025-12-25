package com.example.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.search_service.Repository.ProductsRepository;
import com.example.search_service.Repository.httpClient.PromotionClient;
import com.example.search_service.model.ProductVariants;
import com.example.search_service.viewmodel.CategoryGetVM;
import com.example.search_service.viewmodel.ProductGetListVM;
import com.example.search_service.viewmodel.ProductGetVM;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
import com.example.search_service.viewmodel.dto.UpdatePromotionDTO;
import com.okits02.common_lib.exception.AppException;
import com.example.search_service.exceptions.SearchErrorCode;
import com.example.search_service.mapper.ProductsMapper;
import com.example.search_service.model.Products;
import com.example.search_service.model.Promotion;
import com.example.search_service.viewmodel.dto.ApplyPromotionEventDTO;
import com.example.search_service.viewmodel.dto.StatusPromotionDTO;
import com.example.search_service.viewmodel.dto.request.ApplyThumbnailRequest;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;
    private final CategoryService categoryService;
    private final PromotionClient promotionClient;


    public void createProduct(ProductEventDTO request) throws IOException {
        Products products = productsMapper.toProducts(request);
        var response = promotionClient.getByCategoryIds(request.getCategoriesId());
        products.setPromotions(new HashSet<>(response.getResult()));
        elasticsearchClient.index(i -> i
                .index("product")
                .id(products.getId())
                .document(products)
        );
    }

    public void updateProduct(ProductEventDTO request) {
        Products products = productsRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS));
        boolean different =
                !new HashSet<>(request.getCategoriesId()).equals(new HashSet<>(products.getCategoriesId()));
        if(different){
            var response = promotionClient.getByCategoryIds(request.getCategoriesId());
            products.setPromotions(new HashSet<>(response.getResult()));
        }
        productsMapper.updateProduct(products, request);
        productsRepository.save(products);
    }

    public void deleteProduct(String productId) throws IOException {
        elasticsearchClient.delete(d -> d.index("product").id(productId));
    }

    public void createPromotion(ApplyPromotionEventDTO request) throws IOException {
        Promotion promotion = Promotion.builder()
                .id(request.getId())
                .name(request.getName())
                .descriptions(request.getDescriptions())
                .applyTo(request.getApplyTo())
                .discountPercent(BigDecimal.valueOf(request.getDiscountPercent()))
                .fixedAmount(BigDecimal.valueOf(request.getFixedAmount()))
                .active(request.getActive())
                .createAt(request.getCreateAt())
                .updateAt(request.getUpdateAt())
                .build();
        if(request.getProductIdList() != null) {
            createPromotionByProductId(promotion, request.getProductIdList());
        }
        if(request.getCategoryIdList() != null) {
            createPromotionByCategoryId(promotion, request.getCategoryIdList());
        }
        log.info("create promotion successfully");
    }

    /*
    public void updatePromotion(ApplyPromotionEventDTO request) throws IOException {
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
                operations.add(bulkOperation);
            }
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
     */

    public void updatePromotion(UpdatePromotionDTO request) throws IOException {
        if(request.getApplyTo() != null
                && request.getDeleteApplyTo() != null
                && !request.getDeleteApplyTo().isEmpty()){
            removePromotionInProduct(request.getId(), request.getDeleteApplyTo(), request.getApplyTo());
        }
        if(request.getProductIdList() != null && !request.getProductIdList().isEmpty()) {
            Promotion promotion = Promotion.builder()
                    .id(request.getId())
                    .name(request.getName())
                    .descriptions(request.getDescriptions())
                    .applyTo(request.getApplyTo())
                    .discountPercent(BigDecimal.valueOf(request.getDiscountPercent()))
                    .fixedAmount(BigDecimal.valueOf(request.getFixedAmount()))
                    .active(request.getActive())
                    .createAt(request.getCreateAt())
                    .build();
            createPromotionByProductId(promotion, request.getProductIdList());
        }else if(request.getCategoryIdList() != null && !request.getCategoryIdList().isEmpty()){
            Promotion promotion = Promotion.builder()
                    .id(request.getId())
                    .name(request.getName())
                    .descriptions(request.getDescriptions())
                    .applyTo(request.getApplyTo())
                    .discountPercent(BigDecimal.valueOf(request.getDiscountPercent()))
                    .fixedAmount(BigDecimal.valueOf(request.getFixedAmount()))
                    .active(request.getActive())
                    .createAt(request.getCreateAt())
                    .build();
            createPromotionByCategoryId(promotion, request.getCategoryIdList());
        }
        Query nestedQuery = Query.of(q -> q
                .nested(n -> n
                        .path("promotions")
                        .query(innerQ -> innerQ
                                .term(t -> t
                                        .field("promotions.id")
                                        .value(request.getId())
                                )
                        )
                )
        );

        String scriptSource = """
        for(def promo : ctx._source.promotions){
            if (promo.id.equals(params.promotionId)) {
                if(params.newName != null) promo.name = params.newName;
                if(params.newDescriptions != null) promo.descriptions = params.newDescriptions;
                if(params.newDiscountPercent != null) promo.discountPercent = params.newDiscountPercent;
                if(params.newFixedAmount != null) promo.fixedAmount = params.newFixedAmount;
                if(params.newActive != null) promo.active = params.newActive;
                if(params.newUpdateAt != null) promo.updateAt = params.newUpdateAt;
                break;
            }
        }
        """;

        Map<String, JsonData> params = new HashMap<>();
        if(request.getId() != null) params.put("promotionId", JsonData.of(request.getId()));
        if(request.getName() != null) params.put("newName", JsonData.of(request.getName()));
        if(request.getDescriptions() != null) params.put("newDescriptions", JsonData.of(request.getDescriptions()));
        if(request.getDiscountPercent() != null) params.put("newDiscountPercent", JsonData.of(request.getDiscountPercent()));
        if(request.getFixedAmount() != null) params.put("newFixedAmount", JsonData.of(request.getFixedAmount()));
        if(request.getActive() != null) params.put("newActive", JsonData.of(request.getActive()));
        if(request.getUpdateAt() != null) params.put("newUpdateAt", JsonData.of(request.getUpdateAt()));

        Script script = new Script.Builder()
                .source(scriptSource)
                .lang("painless")
                .params(params)
                .build();

        UpdateByQueryRequest req = UpdateByQueryRequest.of(b -> b
                .index("product")
                .query(nestedQuery) // ✅ Đã thêm query
                .script(script)
        );

        elasticsearchClient.updateByQuery(req);
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
                            .index("product")
                            .id(product.getId())
                            .action(a -> a
                                    .doc(product)
                            )
                    )
            );
            calculatorListPrice(product);
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

    public void createPromotionByCategoryId(Promotion promotion, Set<String> listCategoryId) throws IOException {
        List<BulkOperation> bulkOperationList = new LinkedList<>();
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .terms(t -> t
                                .field("categoriesId")
                                .terms(v -> v
                                        .value(listCategoryId
                                                .stream()
                                                .map(FieldValue::of)
                                                .toList()
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
            BulkOperation bulkOperation = BulkOperation.of(b -> b
                    .update(u -> u
                            .index("product")
                            .id(products.getId())
                            .action(a -> a.doc(products))));
            calculatorListPrice(products);
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
            calculatorListPrice(products);
            operations.add(bulkOperation);
        }
        if (!operations.isEmpty()) {
            BulkRequest bulkRequest = new BulkRequest.Builder()
                    .operations(operations)
                    .build();
            elasticsearchClient.bulk(bulkRequest);
        }
    }

    public void removePromotionInProduct(String promotionId, List<String> deleteApplyTo, String applyTo) throws IOException {
        Query query;
        if("Product".equalsIgnoreCase(applyTo)){
            query = Query.of(q -> q
                    .bool(b -> b
                            .must(m -> m
                                    .nested(n -> n
                                            .path("promotions")
                                            .query(q1 -> q1
                                                    .term(t -> t
                                                            .field("promotions.id")
                                                            .value(promotionId)
                                                    )
                                            )
                                    )
                            )
                            .must(m -> m
                                    .terms(t -> t
                                            .field("id")
                                            .terms(v -> v.value(
                                                    deleteApplyTo.stream()
                                                            .map(FieldValue::of)
                                                            .toList()
                                            ))
                                    )
                            )
                    )
            );
        }else if("Category".equalsIgnoreCase(applyTo)){
            query = Query.of(q -> q
                    .bool(b -> b
                            .must(m -> m
                                    .nested(n -> n
                                            .path("promotions")
                                            .query(q1 -> q1
                                                    .term(t -> t
                                                            .field("promotions.id")
                                                            .value(promotionId)
                                                    )
                                            )
                                    )
                            )
                            .must(m -> m
                                    .terms(t -> t
                                            .field("categoriesId")
                                            .terms(v -> v.value(
                                                    deleteApplyTo.stream()
                                                            .map(FieldValue::of)
                                                            .toList()
                                            ))
                                    )
                            )
                    )
            );
        }else {
            return;
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .build();

        SearchHits<Products> hits =
                elasticsearchOperations.search(searchQuery, Products.class);

        List<BulkOperation> bulkOperations = new ArrayList<>();

        for (SearchHit<Products> hit : hits) {
            Products product = hit.getContent();
            if (product.getPromotions() == null) continue;
            product.getPromotions().removeIf(p -> promotionId.equals(p.getId()));
            calculatorListPrice(product);
            bulkOperations.add(BulkOperation.of(b -> b
                    .update(u -> u
                            .index("product")
                            .id(product.getId())
                            .action(a -> a.doc(product))
                    )
            ));
        }

        if (!bulkOperations.isEmpty()) {
            elasticsearchClient.bulk(b -> b.operations(bulkOperations));
        }
    }
    public void deletePromotion(ApplyPromotionEventDTO request) throws IOException {
        String promotionId = request.getId();
        if (promotionId == null) {
            throw new AppException(SearchErrorCode.ID_OF_PROMOTION_NOT_VALID);
        }
        Query query = Query.of(q -> q
                .nested(n -> n
                        .path("promotions")
                        .query(q1 -> q1
                                .term(t -> t
                                        .field("promotions.id")
                                        .value(promotionId)
                                )
                        )
                )
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .build();

        SearchHits<Products> hits =
                elasticsearchOperations.search(searchQuery, Products.class);

        List<BulkOperation> bulkOperations = new ArrayList<>();

        for (SearchHit<Products> hit : hits) {
            Products product = hit.getContent();
            if (product.getPromotions() == null) continue;
            product.getPromotions().removeIf(p -> promotionId.equals(p.getId()));
            calculatorListPrice(product);
            bulkOperations.add(BulkOperation.of(b -> b
                    .update(u -> u
                            .index("product")
                            .id(product.getId())
                            .action(a -> a.doc(product))
                    )
            ));
        }

        if (!bulkOperations.isEmpty()) {
            elasticsearchClient.bulk(b -> b.operations(bulkOperations));
        }
    }

    public long removeCateInProduct(List<String> categoryId){
        String scriptSource = """
            if (ctx._source.categories != null) {
                ctx._source.categories.removeIf(c -> params.ids.contains(c.id));
            }
        """;
        Map<String, JsonData> params = new HashMap<>();
        params.put("ids", JsonData.of(categoryId));
        var script = new Script.Builder()
                .source(scriptSource)
                .lang("painless")
                .params(params)
                .build();
        UpdateByQueryRequest request = UpdateByQueryRequest.of(u -> u
                .index("product")
                .query(q -> q
                        .nested(n -> n
                                .path("categories")
                                .query(inner -> inner
                                        .terms(t -> t
                                                .field("categories.id")
                                                .terms(terms -> terms
                                                        .value(categoryId.stream().map(FieldValue::of).toList())
                                                )
                                        )
                                )
                        )
                )
                .script(script)
        );
        try{
            UpdateByQueryResponse response = elasticsearchClient.updateByQuery(request);
            log.info("Da update {} documents khi xoa categories: {}", response.updated(), categoryId);
            return response.updated();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void applyThumbnailToProduct(ApplyThumbnailRequest request){
        if(request == null || request.getOwnerId() == null || request.getOwnerId().isBlank()
                || request.getUrl() == null || request.getUrl().isBlank()){
            throw new AppException(SearchErrorCode.INVALID_REQUEST);
        }
        try{
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("product")
                    .id(request.getOwnerId()))
                    .value();

            if(!exists){
                throw new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS);
            }
            elasticsearchClient.update(u -> u
                    .index("product")
                    .id(request.getOwnerId())
                    .doc(Map.of("thumbnail", request.getUrl())),
                    Products.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductGetVM getDetailsProduct(String productId){
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("id")
                                .value(productId)
                        )
                )
                .build();
        SearchHits<Products> hits = elasticsearchOperations.search(nativeQuery, Products.class);
        if(hits.isEmpty()){
            throw new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS);
        }
        Products products = hits.getSearchHit(0).getContent();
        Set<String> categoryIds = new HashSet<>(products.getCategoriesId());
        Map<String, CategoryGetVM> categoryMap =
                categoryService.getCategoryByIds(categoryIds);
        ProductGetVM productGetVM = ProductGetVM.fromEntity(products, categoryMap);
        return productGetVM;
    }

    public ProductGetListVM getByListIds(List<String> productIds, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .terms(t -> t
                                .field("id")
                                .terms(v -> v
                                        .value(productIds
                                                .stream()
                                                .map(FieldValue::of)
                                                .toList()
                                        )
                                )
                        )
                ).withPageable(pageable).build();
        SearchHits<Products> productsSearchHits = elasticsearchOperations.search(nativeQuery, Products.class);
        SearchPage<Products> productsSearchPage = SearchHitSupport.searchPageFor(
                productsSearchHits, nativeQuery.getPageable());
        List<Products> products = productsSearchHits.stream().map(SearchHit::getContent).toList();
        Set<String> categoryIds = products.stream()
                .flatMap(p -> p.getCategoriesId().stream())
                .collect(Collectors.toSet());
        Map<String, CategoryGetVM> categoryMap =
                categoryService.getCategoryByIds(categoryIds);
        List<ProductGetVM> productGetVMList = productsSearchHits.stream().map(i -> ProductGetVM
                .fromEntity(i.getContent(), categoryMap)).toList();
        return ProductGetListVM.<ProductGetVM>builder()
                .productGetVMList(productGetVMList)
                .currentPages(productsSearchPage.getNumber())
                .totalPage(productsSearchPage.getTotalPages())
                .pageSize(productsSearchPage.getSize())
                .totalElements(productsSearchPage.getTotalElements())
                .build();
    }

    private void calculatorListPrice(Products products){

        if (products.getProductVariants() == null) return;

        List<Promotion> promotions = new ArrayList<>(products.getPromotions());

        for (ProductVariants variant : products.getProductVariants()) {
            variant.setSellPrice(
                    calculateSellPriceForVariant(
                            variant.getPrice(),
                            promotions
                    )
            );
        }
    }

    private BigDecimal calculateSellPriceForVariant(
            BigDecimal price,
            List<Promotion> promotions
    ){
        if(price == null){return BigDecimal.ZERO;}
        if(promotions == null || promotions.isEmpty()){return price;}

        BigDecimal result = price;

        for(Promotion promotion : promotions){
            if(Boolean.TRUE.equals(promotion.getActive()) && promotion.getDiscountPercent() != null){
                BigDecimal percent = promotion.getDiscountPercent()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                result = result.subtract(result.multiply(percent));
            }

            if(Boolean.TRUE.equals(promotion.getActive()) && promotion.getFixedAmount() != null){
                result = result.subtract(promotion.getFixedAmount());
            }
        }
        return result.max(BigDecimal.ZERO);
    }
}
