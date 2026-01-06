    package com.example.search_service.service;

    import co.elastic.clients.elasticsearch._types.FieldValue;
    import co.elastic.clients.elasticsearch._types.SortMode;
    import co.elastic.clients.elasticsearch._types.SortOrder;
    import co.elastic.clients.elasticsearch._types.aggregations.*;
    import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
    import com.example.search_service.exceptions.SearchErrorCode;
    import com.example.search_service.model.ProductVariants;
    import com.example.search_service.viewmodel.dto.AutoCompletedResponse;
    import com.okits02.common_lib.exception.AppException;
    import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

    import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
    import com.example.search_service.constant.SortType;
    import com.example.search_service.model.Category;
    import com.example.search_service.model.Products;
    import com.example.search_service.viewmodel.*;
    import com.example.search_service.viewmodel.dto.SpecificationFilterDTO;
    import com.example.search_service.viewmodel.dto.request.AdminSearchRequest;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Sort;
    import org.springframework.data.elasticsearch.client.elc.NativeQuery;
    import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
    import org.springframework.data.elasticsearch.core.*;
    import org.springframework.stereotype.Service;

    import java.util.*;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class SearchService {
        private final ElasticsearchOperations elasticsearchOperations;
        private final CategoryService categoryService;

        public ProductGetListVM searchProductAdvance(String keyword,
                                                     Integer page,
                                                     Integer size,
                                                     String brandName,
                                                     String category,
                                                     List<SpecificationFilterDTO> attributes,
                                                     Double minPrice,
                                                     Double maxPrice,
                                                     SortType sortType)
        {
            NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder();
            nativeQueryBuilder.withQuery(q -> q.bool(b -> {
                if (keyword != null && !keyword.isBlank()) {
                    b.must(m -> m.match(match -> match
                            .field("name")
                            .query(keyword)
                    ));
                }
                return b;
            }));
            nativeQueryBuilder.withFilter(f -> f.bool(b -> {
                extractCategory(category, b);
                extractAttributes(attributes, "specifications", b);
                extractRange(minPrice, maxPrice, b);
                extractBrandName(brandName, "brand", b);
                return b;
            }));
            switch (sortType)
            {
                case DEFAULT -> {
                    break;
                }
                case PRICE_ASC -> {
                    nativeQueryBuilder.withSort(s -> s
                            .field(f -> f
                                    .field("productVariants.sellPrice")
                                    .order(SortOrder.Asc)
                                    .nested(n -> n.path("productVariants"))
                                    .mode(SortMode.Min)
                            )
                    );
                }
                case PRICE_DESC -> {
                    nativeQueryBuilder.withSort(s -> s
                            .field(f -> f
                                    .field("productVariants.sellPrice")
                                    .order(SortOrder.Desc)
                                    .nested(n -> n.path("productVariants"))
                                    .mode(SortMode.Max)
                            )
                    );
                }
                case RATING_ASC -> {
                    nativeQueryBuilder.withSort(Sort.by(Sort.Direction.ASC, "avgRating"));
                    break;
                }
            }
            buildAggregations(nativeQueryBuilder);
            log.error("ES QUERY = {}", nativeQueryBuilder.getQuery());
            nativeQueryBuilder.withPageable(PageRequest.of(page, size));
            SearchHits<Products> productsSearchHits = elasticsearchOperations.search(nativeQueryBuilder.build(), Products.class);
            SearchPage<Products> productsSearchPage = SearchHitSupport.searchPageFor(
                    productsSearchHits, nativeQueryBuilder.getPageable());
            log.info("search hits", productsSearchHits.get());
            List<ProductSummariseVM> productGetVMList = productsSearchHits.stream().map(i -> ProductSummariseVM
                    .fromEntity(i.getContent())).toList();
            Map<String, Map<String, Long>> techAggregations = getAggregationTech(productsSearchHits);
            Map<String, Long> categoriesAggregations = getAggregationsCategories(productsSearchHits);
            return ProductGetListVM.<ProductSummariseVM>builder()
                    .productGetVMList(productGetVMList)
                    .currentPages(productsSearchPage.getNumber())
                    .totalPage(productsSearchPage.getTotalPages())
                    .pageSize(productsSearchPage.getSize())
                    .totalElements(productsSearchPage.getTotalElements())
                    .specificationAggregations(techAggregations)
                    .categoriesAggregations(categoriesAggregations)
                    .build();
        }

        public ProductGetVM getProductById(String id){
            NativeQueryBuilder query = NativeQuery.builder()
                    .withQuery(q -> q
                            .term(t -> t
                                    .field("_id")
                                    .value(id)
                            )
                    );
            SearchHits<Products> hits = elasticsearchOperations.search(query.build(), Products.class);
            if (hits.isEmpty()) {
                throw new AppException(SearchErrorCode.PRODUCT_NOT_EXISTS);
            }

            Products product = hits.getSearchHit(0).getContent();
            Map<String, CategoryGetVM> categoryMap =
                    categoryService.getCategoryByIds(new HashSet<>(product.getCategoriesId()));
            return ProductGetVM.fromEntity(product, categoryMap);
        }

        public ProductGetListVM searchProductAdmin(int page, int size, AdminSearchRequest request){
            NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder();
            nativeQueryBuilder.withQuery(q -> q.bool(b -> {
                if(request.getProductName() != null && !request.getProductName().isBlank()){
                    b.must(m -> m.match(match -> match
                            .field("name")
                            .query(request.getProductName()
                            )
                    )
                    );
                }
                return b;
            }));
            nativeQueryBuilder.withPageable(PageRequest.of(page, size));
            SearchHits<Products> searchHits = elasticsearchOperations.search(nativeQueryBuilder.build(), Products.class);
            SearchPage<Products> productsSearchPage = SearchHitSupport.searchPageFor(
                    searchHits, nativeQueryBuilder.getPageable());
            List<Products> products = searchHits.stream().map(SearchHit::getContent).toList();
            Set<String> categoryIds = products.stream()
                    .flatMap(p -> p.getCategoriesId().stream())
                    .collect(Collectors.toSet());
            Map<String, CategoryGetVM> categoryMap =
                    categoryService.getCategoryByIds(categoryIds);
            List<ProductGetVM> productGetVMList = searchHits.stream().map(i -> ProductGetVM
                    .fromEntity(i.getContent(), categoryMap)).toList();
            return ProductGetListVM.<ProductGetVM>builder()
                    .productGetVMList(productGetVMList)
                    .currentPages(productsSearchPage.getNumber())
                    .totalPage(productsSearchPage.getTotalPages())
                    .pageSize(productsSearchPage.getSize())
                    .totalElements(productsSearchPage.getTotalElements())
                    .build();
        }

        private void extractAttributes(List<SpecificationFilterDTO> attributes,
                                       String productField, BoolQuery.Builder b) {
            if (attributes == null || attributes.isEmpty()) return;

            for (SpecificationFilterDTO attr : attributes) {
                    b.must(m -> m.nested(n -> n
                            .path(productField)
                            .query(q -> q.bool(bl -> bl
                                    .must(ms -> ms.term(t -> t
                                            .field("specifications.key")
                                            .value(attr.getKey())
                                    ))
                                    .must(ms -> ms.term(t -> t
                                            .field("specifications.value")
                                            .value(attr.getValue())
                                    ))
                            ))
                    ));
            }
        }

        public Map<String, Map<String, Long>> getAggregationTech(SearchHits<Products> hits){
            Map<String, Map<String, Long>> result = new HashMap<>();

            AggregationsContainer<?> container = hits.getAggregations();
            if (container == null) return result;

            List<ElasticsearchAggregation> aggs =
                    (List<ElasticsearchAggregation>) container.aggregations();

            ElasticsearchAggregation techAggWrapper = aggs.stream()
                    .filter(a -> "tech_specs".equals(a.aggregation().getName()))
                    .findFirst()
                    .orElse(null);

            if (techAggWrapper == null) return result;

            Aggregate aggregate = techAggWrapper.aggregation().getAggregate();
            NestedAggregate nested = aggregate.nested();

            StringTermsAggregate byGroup =
                    nested.aggregations().get("only_tech").sterms();

            for(StringTermsBucket groupBucket : byGroup.buckets().array()) {
                String group = groupBucket.key().stringValue();
                Map<String, Long> valueMap = new HashMap<>();
                StringTermsAggregate byKey =
                        groupBucket.aggregations().get("by_key").sterms();

                for (StringTermsBucket keyBucket : byKey.buckets().array()) {

                    StringTermsAggregate byValue =
                            keyBucket.aggregations().get("by_value").sterms();

                    for (StringTermsBucket valueBucket : byValue.buckets().array()) {
                        valueMap.merge(
                                valueBucket.key().stringValue(),
                                valueBucket.docCount(),
                                Long::sum
                        );
                    }
                }
                result.put(group, valueMap);
            }

            return result;
        }

        public Map<String, Long> getAggregationsCategories(SearchHits<Products> hits){
            Map<String, Long> result = new HashMap<>();
            AggregationsContainer<?> container = hits.getAggregations();
            if(container == null) return result;
            List<ElasticsearchAggregation> aggs = (List<ElasticsearchAggregation>) container.aggregations();
            ElasticsearchAggregation cateAggWrapper = aggs.stream()
                    .filter(a -> "categories".equals(a.aggregation().getName()))
                    .findFirst()
                    .orElse(null);
            if (cateAggWrapper == null) return result;
            StringTermsAggregate byCate = cateAggWrapper.aggregation().getAggregate().sterms();
            for(StringTermsBucket valueBuket : byCate.buckets().array()){
                result.put(
                        valueBuket.key().stringValue(),
                        valueBuket.docCount()
                );
            }
            return result;
        }
        private void extractCategory(String category, BoolQuery.Builder b)
        {
            if(category == null || category.isEmpty())
            {
                return;
            }
            b.must(m -> m
                    .terms(t -> t
                            .field("categoriesId")
                            .terms(v -> v.value(List.of(FieldValue.of(category))))
                    )
            );
        }

        private void extractRange(Number min, Number max, BoolQuery.Builder b) {
            if (min == null && max == null) return;

            b.must(m -> m.nested(n -> n
                    .path("productVariants")
                    .query(q -> q.range(r -> r
                            .number(num -> {
                                num.field("productVariants.sellPrice");
                                if (min != null) num.gte(min.doubleValue());
                                if (max != null) num.lte(max.doubleValue());
                                return num;
                            })
                    ))
            ));
        }

        private void extractBrandName(String strField, String productField, BoolQuery.Builder b){
            if (strField == null || strField.isBlank()) return;

            String[] strFields = strField.split(",");

            b.must(m -> m.bool(bb -> {
                for (String str : strFields) {
                    bb.should(s -> s
                            .term(t -> t
                                    .field(productField)
                                    .value(str)
                            )
                    );
                }
                bb.minimumShouldMatch("1");
                return bb;
            }));
        }
        private void buildAggregations(NativeQueryBuilder builder) {
            Aggregation techAgg = Aggregation.of(a -> a
                    .nested(n -> n.path("specifications"))
                    .aggregations("only_tech", Aggregation.of(a2 -> a2
                            .terms(t -> t
                                    .field("specifications.group")
                                    .size(20)
                            )
                            .aggregations("by_key", Aggregation.of(a3 -> a3
                                    .terms(t -> t
                                            .field("specifications.key")
                                            .size(50)
                                    )
                                    .aggregations("by_value", Aggregation.of(a4 -> a4
                                            .terms(t2 -> t2
                                                    .field("specifications.value")
                                                    .size(50)
                                            )
                                    ))
                            ))
                    ))
            );
            builder.withAggregation("tech_specs", techAgg);

            Aggregation cateAgg = Aggregation.of(a -> a
                    .terms(t -> t
                            .field("categoriesId")
                            .size(20)
                    )
            );
            builder.withAggregation("categories", cateAgg);
        }



        public List<AutoCompletedResponse> autocompleteCategory(String prefix, int limit) {;
            if(prefix == null || prefix.trim().length() < 2){
                return List.of();
            }

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(m -> m
                                    .query(prefix)
                                    .type(TextQueryType.BoolPrefix)
                                    .fields("name",
                                            "name._2gram",
                                            "name._3gram")
                            )
                    )
                    .withMaxResults(limit)
                    .build();

            SearchHits<Category> hits = elasticsearchOperations.search(nativeQuery, Category.class);
            return hits.getSearchHits().stream()
                    .map(hit -> new AutoCompletedResponse(
                            "CATEGORY",
                            hit.getContent().getId(),
                            hit.getContent().getName()
                    ))
                    .toList();
        }

        public List<AutoCompletedResponse> autoCompletedProductQuick(String prefix, int limit){
            if (prefix == null || prefix.trim().length() < 2) {
                return List.of();
            }

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(m -> m
                                    .query(prefix)
                                    .type(TextQueryType.BoolPrefix)
                                    .fields(
                                            "name",
                                            "name._2gram",
                                            "name._3gram"
                                    )
                            )
                    )
                    .withMaxResults(limit)
                    .build();
            SearchHits<Products> hits = elasticsearchOperations.search(query, Products.class);
            return hits.getSearchHits().stream()
                    .map(hit -> new AutoCompletedResponse(
                            "PRODUCT",
                            hit.getContent().getId(),
                            hit.getContent().getName()
                    ))
                    .toList();
        }

        public List<AutoCompletedResponse> autocompleteFull(
                String keyword
        ) {
            if (keyword == null || keyword.trim().length() < 2) {
                return List.of();
            }

            List<AutoCompletedResponse> result = new ArrayList<>();

            result.addAll(autoCompletedProductQuick(keyword, 5));
            result.addAll(autocompleteCategory(keyword, 5));

            return result;
        }

        public List<ProductSkuVM> getProductBySku(List<String> skus){
            if (skus == null || skus.isEmpty()) {
                return List.of();
            }

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .nested(n -> n
                                    .path("productVariants")
                                    .query(nq -> nq
                                            .terms(t -> t
                                                    .field("productVariants.sku")
                                                    .terms(v -> v.value(
                                                            skus.stream()
                                                                    .map(FieldValue::of)
                                                                    .toList()
                                                    ))
                                            )
                                    )
                                    .innerHits(ih -> ih
                                            .size(skus.size())
                                    )
                            )
                    )
                    .build();

            SearchHits<Products> hits =
                    elasticsearchOperations.search(query, Products.class);

            if (hits.isEmpty()) {
                return List.of();
            }

            List<ProductSkuVM> result = new ArrayList<>();

            for (SearchHit<Products> hit : hits) {
                Products product = hit.getContent();

                SearchHits<?> variantHits =
                        hit.getInnerHits().get("productVariants");

                if (variantHits == null) {
                    continue;
                }

                for (SearchHit<?> variantHit : variantHits) {
                    ProductVariants variant =
                            (ProductVariants) variantHit.getContent();

                    if (!skus.contains(variant.getSku())) {
                        continue;
                    }

                    result.add(ProductSkuVM.fromEntity(product, variant.getSku()));
                }
            }

            return result;
        }
    }
