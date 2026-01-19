package com.example.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.search_service.repository.ProductsRepository;
import com.example.search_service.repository.httpClient.PromotionClient;
import com.example.search_service.model.ProductVariants;
import com.example.search_service.viewmodel.CateItemDTO;
import com.example.search_service.viewmodel.CategoryGetVM;
import com.example.search_service.viewmodel.ProductGetListVM;
import com.example.search_service.viewmodel.ProductGetVM;
import com.example.search_service.viewmodel.dto.*;
import com.okits02.common_lib.exception.AppException;
import com.example.search_service.exceptions.SearchErrorCode;
import com.example.search_service.mapper.ProductsMapper;
import com.example.search_service.model.Products;
import com.example.search_service.model.Promotion;
import com.example.search_service.viewmodel.dto.request.ApplyThumbnailRequest;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

        public void createProduct(ProductEventDTO request) {
          if (productsRepository.existsById(request.getId())) {
            log.warn("Product already exists, skip indexing. productId={}", request.getId());
            return;
          }

          Products products = productsMapper.toProducts(request);
          products.setSold(0L);
          products.setCategoriesId(request.getCategories().stream().map(CateItemDTO::getId).toList());
          try {
            if (request.getCategories() != null && !request.getCategories().isEmpty()) {
              var response = promotionClient.getByCategoryIds(request.getCategories()
                      .stream()
                      .map(CateItemDTO::getId).toList());
              products.setPromotions(
                response.getResult() != null ?
                new HashSet < > (response.getResult()) :
                Set.of());
            } else {
              products.setPromotions(Set.of());
            }
          } catch (Exception e) {
            log.error("Product create error search service. productId={}",
              request.getId(), e);
            products.setPromotions(Set.of());
          }

          calculatorListPrice(products);

          try {
            elasticsearchClient.index(i -> i
              .index("product")
              .id(products.getId())
              .document(products)
              .refresh(Refresh.True));

            log.info("ES INDEX SUCCESS productId={}", products.getId());

          } catch (Exception e) {
            log.error("ES INDEX FAILED productId={}", products.getId(), e);
          }
        }

        public void updateProduct(ProductEventDTO request) {
          Products product = productsRepository.findById(request.getId())
            .orElseThrow(() -> new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS));

          if (isCategoryChanged(request.getCategories().stream().map(CateItemDTO::getId).toList()
                  , product.getCategoriesId())) {
            try {
              if (request.getAvgRating() != null && !request.getCategories().isEmpty()) {
                var response = promotionClient.getByCategoryIds(request.getCategories().stream()
                        .map(CateItemDTO::getId).toList());
                product.setPromotions(
                  response.getResult() != null ?
                  new HashSet < > (response.getResult()) :
                  Set.of());
              } else {
                product.setPromotions(Set.of());
              }
            } catch (Exception e) {
              log.error("Promotion service unavailable, keep old promotions. productId={}",
                product.getId(), e);
            }
          }

          productsMapper.updateProduct(product, request);
            List<String> newCategoryIds = request.getCategories() == null
                    ? List.of()
                    : request.getCategories()
                    .stream()
                    .map(CateItemDTO::getId)
                    .toList();
            product.setCategoriesId(new ArrayList<>(newCategoryIds));
          product.setProductVariants(request.getProductVariants());
          calculatorListPrice(product);

          productsRepository.save(product);

          try {
            elasticsearchClient.index(i -> i
              .index("product")
              .id(product.getId())
              .document(product)
              .refresh(Refresh.True));
            log.info("ES UPDATE SUCCESS productId={}", product.getId());
          } catch (Exception e) {
            log.error("ES UPDATE FAILED productId={}", product.getId(), e);
          }
        }

        public void deleteProduct(String productId) {
          try {
            DeleteResponse response = elasticsearchClient.delete(d -> d
              .index("product")
              .id(productId)
              .refresh(Refresh.True));

            log.warn("ES DELETE productId={}, result={}",
              productId, response.result());

          } catch (Exception e) {
            log.error("ES DELETE FAILED productId={}", productId, e);
          }
        }

    public void deleteProductByList(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            log.warn("ES BULK DELETE: productIds is empty");
            return;
        }

        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

            for (String productId : productIds) {
                bulkBuilder.operations(op -> op
                        .delete(d -> d
                                .index("product")
                                .id(productId)
                        )
                );
            }

            BulkResponse response = elasticsearchClient.bulk(bulkBuilder.build());

            if (response.errors()) {
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error(
                                "ES BULK DELETE FAILED id={}, reason={}",
                                item.id(),
                                item.error().reason()
                        );
                    }
                });
            } else {
                log.warn("ES BULK DELETE SUCCESS, total={}", productIds.size());
            }

        } catch (Exception e) {
            log.error("ES BULK DELETE EXCEPTION", e);
        }
    }

    public void deleteAllProduct() {
        try {
            DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(d -> d
                    .index("product")
                    .query(q -> q.matchAll(m -> m))
                    .refresh(true)
            );

            log.warn("ES DELETE ALL PRODUCT: deleted={}", response.deleted());

        } catch (Exception e) {
            log.error("ES DELETE ALL PRODUCT FAILED", e);
        }
    }

        public void createPromotion(ApplyPromotionEventDTO request) throws IOException {
            BigDecimal discountPercent = null;
            BigDecimal fixedAmount = null;

            if (request.getDiscountPercent() != null) {
                discountPercent = BigDecimal.valueOf(request.getDiscountPercent());
            }

            if (request.getFixedAmount() != null ) {
                fixedAmount = BigDecimal.valueOf(request.getFixedAmount());
            }
            Promotion promotion = Promotion.builder()
                    .id(request.getId())
                    .name(request.getName())
                    .campaignId(request.getCampaignId())
                    .descriptions(request.getDescriptions())
                    .applyTo(request.getApplyTo())
                    .discountPercent(discountPercent)
                    .fixedAmount(fixedAmount)
                    .promotionKind(request.getPromotionKind())
                    .active(request.getActive())
                    .createAt(request.getCreateAt())
                    .updateAt(request.getUpdateAt())
                    .build();
            if (request.getProductIdList() != null) {
                createPromotionByProductId(promotion, request.getProductIdList());
            }
            if (request.getCategoryIdList() != null) {
                createPromotionByCategoryId(promotion, request.getCategoryIdList());
            }
            log.info("create promotion successfully");
        }

        public void createPromotionByProductId(Promotion promotion, Set<String> listProductId) throws IOException {
                List<BulkOperation> operations = new ArrayList<>();
                NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q.terms(t -> t
                            .field("id")
                            .terms(v -> v.value(
                                    listProductId.stream()
                                            .map(FieldValue::of)
                                            .toList()
                            ))
                    ))
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
                                                                        .doc(product))));
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
                                                                                                .toList()))))
                                .build();
                SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQuery, Products.class);
                for (SearchHit<Products> hit : searchHits) {
                        Products products = hit.getContent();
                        if (products.getPromotions() == null) {
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

        public void deletePromotion(String promotionId) throws IOException {
                if (promotionId == null) {
                        throw new AppException(SearchErrorCode.ID_OF_PROMOTION_NOT_VALID);
                }
                Query query = Query.of(q -> q
                    .term(t -> t
                            .field("promotions.id")
                            .value(promotionId)
                    )
                );

                NativeQuery searchQuery = NativeQuery.builder()
                                .withQuery(query)
                                .build();

                SearchHits<Products> hits = elasticsearchOperations.search(searchQuery, Products.class);

                List<BulkOperation> bulkOperations = new ArrayList<>();

                for (SearchHit<Products> hit : hits) {
                        Products product = hit.getContent();
                        if (product.getPromotions() == null)
                                continue;
                        product.getPromotions().removeIf(p -> promotionId.equals(p.getId()));
                        calculatorListPrice(product);
                        bulkOperations.add(BulkOperation.of(b -> b
                                        .update(u -> u
                                                        .index("product")
                                                        .id(product.getId())
                                                        .action(a -> a.doc(product)))));
                }

                if (!bulkOperations.isEmpty()) {
                        elasticsearchClient.bulk(b -> b.operations(bulkOperations));
                }
        }

        public long removeCateInProduct(List<String> categoryId) {
                String scriptSource = """
                                    if (ctx._source.categoriesId != null) {
                                        ctx._source.categoriesId.removeIf(c -> params.ids.contains(c));
                                    }
                                """;
                Map<String, JsonData> params = new HashMap<>();
                params.put("ids", JsonData.of(categoryId));

                Script script = new Script.Builder()
                                .source(scriptSource)
                                .lang("painless")
                                .params(params)
                                .build();

                UpdateByQueryRequest request = UpdateByQueryRequest.of(u -> u
                                .index("product")
                                .query(q -> q
                                                .terms(t -> t
                                                                .field("categoriesId")
                                                                .terms(v -> v.value(
                                                                                categoryId.stream()
                                                                                                .map(FieldValue::of)
                                                                                                .toList()))))
                                .script(script));

                try {
                        UpdateByQueryResponse response = elasticsearchClient.updateByQuery(request);
                        log.info("Updated {} documents, removed categories: {}", response.updated(), categoryId);
                        return response.updated();
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        public void applyThumbnailToProduct(ApplyThumbnailRequest request) {
                if (request == null
                                || request.getOwnerId() == null || request.getOwnerId().isBlank()
                                || request.getOwnerId() == null || request.getOwnerId().isBlank()
                                || request.getUrl() == null || request.getUrl().isBlank()) {
                        throw new AppException(SearchErrorCode.INVALID_REQUEST);
                }

                try {
                        boolean exists = elasticsearchClient.exists(e -> e
                                        .index("product")
                                        .id(request.getProductId())).value();

                        if (!exists) {
                                throw new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS);
                        }
                        elasticsearchClient.update(u -> u
                                        .index("product")
                                        .id(request.getProductId())
                                        .script(s -> s
                                                        .lang("painless")
                                                        .source(
                                                                        """
                                                                                                if (ctx._source.productVariants != null) {
                                                                                                    for (int i = 0; i < ctx._source.productVariants.size(); i++) {
                                                                                                        if (ctx._source.productVariants[i].sku == params.sku) {
                                                                                                            ctx._source.productVariants[i].thumbnail = params.url;
                                                                                                            break;
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                        """)
                                                        .params(Map.of(
                                                                        "sku", JsonData.of(request.getOwnerId()),
                                                                        "url", JsonData.of(request.getUrl())))),
                                        Products.class);

                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

        public void publishRating(RatingEventDTO ratingEventDTO) throws IOException {
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("product")
                    .id(ratingEventDTO.getProductId())).value();

            if (!exists) {
                throw new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS);
            }
            final Double avgRating = Math.max(
                    0.0,
                    Math.min(
                            ratingEventDTO.getAvgRating() != null
                                    ? ratingEventDTO.getAvgRating()
                                    : 0.0,
                            5.0
                    )
            );
            elasticsearchClient.update(
                    u -> u
                            .index("product")
                            .id(ratingEventDTO.getProductId())
                            .doc(Map.of("avgRating", avgRating)),
                    Products.class
            );
        }

        public ProductGetVM getDetailsProduct(String productId) {
                NativeQuery nativeQuery = NativeQuery.builder()
                                .withQuery(q -> q
                                                .term(t -> t
                                                                .field("id")
                                                                .value(productId)))
                                .build();
                SearchHits<Products> hits = elasticsearchOperations.search(nativeQuery, Products.class);
                if (hits.isEmpty()) {
                        throw new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS);
                }
                Products products = hits.getSearchHit(0).getContent();
                Set<String> categoryIds = new HashSet<>(products.getCategoriesId());
                Map<String, CategoryGetVM> categoryMap = categoryService.getCategoryByIds(categoryIds);
                ProductGetVM productGetVM = ProductGetVM.fromEntity(products, categoryMap);
                return productGetVM;
        }

        public ProductGetListVM getByListIds(List<String> productIds, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                NativeQuery nativeQuery = NativeQuery.builder()
                                .withQuery(q -> q
                                                .terms(t -> t
                                                                .field("id")
                                                                .terms(v -> v
                                                                                .value(productIds
                                                                                                .stream()
                                                                                                .map(FieldValue::of)
                                                                                                .toList()))))
                                .withPageable(pageable).build();
                SearchHits<Products> productsSearchHits = elasticsearchOperations.search(nativeQuery, Products.class);
                SearchPage<Products> productsSearchPage = SearchHitSupport.searchPageFor(
                                productsSearchHits, nativeQuery.getPageable());
                List<Products> products = productsSearchHits.stream().map(SearchHit::getContent).toList();
                Set<String> categoryIds = products.stream()
                                .flatMap(p -> p.getCategoriesId().stream())
                                .collect(Collectors.toSet());
                Map<String, CategoryGetVM> categoryMap = categoryService.getCategoryByIds(categoryIds);
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

        private void calculatorListPrice(Products products) {

                if (products.getProductVariants() == null)
                        return;

                List<Promotion> promotions = new ArrayList<>(products.getPromotions());

                for (ProductVariants variant : products.getProductVariants()) {
                        variant.setSellPrice(
                                        calculateSellPriceForVariant(
                                                        variant.getPrice(),
                                                        promotions));
                }
        }

        private BigDecimal calculateSellPriceForVariant(
                        BigDecimal price,
                        List<Promotion> promotions) {
                if (price == null) {
                        return BigDecimal.ZERO;
                }
                if (promotions == null || promotions.isEmpty()) {
                        return price;
                }

                BigDecimal result = price;

                for (Promotion promotion : promotions) {
                        if (Boolean.TRUE.equals(promotion.getActive()) && promotion.getDiscountPercent() != null) {
                                BigDecimal percent = promotion.getDiscountPercent()
                                                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                                result = result.subtract(result.multiply(percent));
                        }

                        if (Boolean.TRUE.equals(promotion.getActive()) && promotion.getFixedAmount() != null) {
                                result = result.subtract(promotion.getFixedAmount());
                        }
                }
                return result.max(BigDecimal.ZERO);
        }

        private Boolean isCategoryChanged(
                        List<String> newCategory,
                        List<String> oldCategory) {
                if (newCategory == null && oldCategory == null) {
                        return false;
                }
                if (newCategory == null || oldCategory == null) {
                        return true;
                }
                return !new HashSet<>(newCategory)
                                .equals(new HashSet<>(oldCategory));
        }

    public void changeSold(String sku, String transaction, Integer quantity) {
        if (sku == null || transaction == null || quantity == null || quantity <= 0) {
            return;
        }
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .nested(n -> n
                                .path("productVariants")
                                .query(nq -> nq
                                        .term(t -> t
                                                .field("productVariants.sku")
                                                .value(sku)
                                        )
                                )
                        )
                )
                .withMaxResults(1)
                .build();

        SearchHits<Products> hits =
                elasticsearchOperations.search(query, Products.class);

        if (hits.isEmpty()) {
            log.warn("Không tìm thấy product với sku={}", sku);
            return;
        }
        Products product = hits.getSearchHit(0).getContent();

        Long currentSold = product.getSold() == null ? 0L : product.getSold();
        if ("OUT".equalsIgnoreCase(transaction)) {
            product.setSold(currentSold + quantity);
        } else if ("RETURN".equalsIgnoreCase(transaction)) {
            product.setSold(Math.max(0, currentSold - quantity));
        }
        elasticsearchOperations.save(product);
    }

    public void changeStockRequest(String sku, Boolean isStock) throws IOException {
        elasticsearchClient.updateByQuery(u -> u
                .index("product")
                .query(q -> q
                        .nested(n -> n
                                .path("productVariants")
                                .query(nq -> nq
                                        .term(t -> t
                                                .field("productVariants.sku")
                                                .value(sku)
                                        )
                                )
                        )
                )
                .script(s -> s
                        .lang("painless")
                        .source(
                                "for (variant in ctx._source.productVariants) {" +
                                        " if (variant.sku == params.sku) {" +
                                        "   variant.isStock = params.isStock;" +
                                        " }" +
                                        "}"
                        )
                        .params("sku", JsonData.of(sku))
                        .params("isStock", JsonData.of(isStock))
                )
        );
    }
}
