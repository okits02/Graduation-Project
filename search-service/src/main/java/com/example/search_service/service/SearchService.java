    package com.example.search_service.service;

    import co.elastic.clients.elasticsearch._types.FieldValue;
    import co.elastic.clients.elasticsearch._types.SortMode;
    import co.elastic.clients.elasticsearch._types.SortOrder;
    import co.elastic.clients.elasticsearch._types.aggregations.*;
    import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
    import com.example.search_service.viewmodel.dto.AutoCompletedResponse;
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
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.data.elasticsearch.client.elc.NativeQuery;
    import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
    import org.springframework.data.elasticsearch.core.*;
    import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
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
            buildSpecAggregations(nativeQueryBuilder);
            log.error("ES QUERY = {}", nativeQueryBuilder.getQuery());
            nativeQueryBuilder.withPageable(PageRequest.of(page, size));
            SearchHits<Products> productsSearchHits = elasticsearchOperations.search(nativeQueryBuilder.build(), Products.class);
            SearchPage<Products> productsSearchPage = SearchHitSupport.searchPageFor(
                    productsSearchHits, nativeQueryBuilder.getPageable());
            log.info("search hits", productsSearchHits.get());
            List<ProductSummariseVM> productGetVMList = productsSearchHits.stream().map(i -> ProductSummariseVM
                    .fromEntity(i.getContent())).toList();
            Map<String, Map<String, Long>> techAggregations = getAggregationTech(productsSearchHits);
            //Map<String, Map<String, Long>> variantSpecAggregations = getAggregationsVariants(productsSearchHits);
            return ProductGetListVM.<ProductSummariseVM>builder()
                    .productGetVMList(productGetVMList)
                    .currentPages(productsSearchPage.getNumber())
                    .totalPage(productsSearchPage.getTotalPages())
                    .pageSize(productsSearchPage.getSize())
                    .totalElements(productsSearchPage.getTotalElements())
                    .aggregations(techAggregations)
                    .build();
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
                if ("TECH".equals(attr.getType())) {
                    b.must(m -> m.nested(n -> n
                            .path("specifications")
                            .query(q -> q.bool(bl -> bl
                                    .must(ms -> ms.term(t -> t
                                            .field("specifications.key")
                                            .value(attr.getKey())
                                    ))
                                    .must(ms -> ms.term(t -> t
                                            .field("specifications.value")
                                            .value(attr.getValue())
                                    ))
                                    .must(ms -> ms.term(t -> t
                                            .field("specifications.type")
                                            .value("tech")
                                    ))
                            ))
                    ));
                }
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

            FilterAggregate onlyTech =
                    nested.aggregations().get("only_tech").filter();

            StringTermsAggregate byKey =
                    onlyTech.aggregations().get("by_key").sterms();

            for (StringTermsBucket keyBucket : byKey.buckets().array()) {

                String key = keyBucket.key().stringValue();
                StringTermsAggregate byValue =
                        keyBucket.aggregations().get("by_value").sterms();

                Map<String, Long> valueMap = new HashMap<>();
                for (StringTermsBucket valueBucket : byValue.buckets().array()) {
                    valueMap.put(
                            valueBucket.key().stringValue(),
                            valueBucket.docCount()
                    );
                }
                result.put(key, valueMap);
            }

            return result;
        }

//        public Map<String, Map<String, Long>> getAggregationsVariants(SearchHits<Products> hits){
//            Map<String, Map<String, Long>> result = new HashMap<>();
//            AggregationsContainer<?> container = hits.getAggregations();
//            if (container == null) {
//                return result;
//            }
//
//            List<ElasticsearchAggregation> aggs =
//                    (List<ElasticsearchAggregation>) container.aggregations();
//
//            ElasticsearchAggregation variantAggWrapper = aggs.stream()
//                    .filter(a -> "variant_specs".equals(a.aggregation().getName()))
//                    .findFirst()
//                    .orElse(null);
//
//            if (variantAggWrapper == null) return result;
//
//            Aggregate variantAgg = variantAggWrapper.aggregation().getAggregate();
//            if (!variantAgg.isNested()) return result;
//            NestedAggregate variantNested = variantAgg.nested();
//            Aggregate bestSpecsAgg = variantNested.aggregations().get("best_specs");
//            if (bestSpecsAgg == null || !bestSpecsAgg.isNested()) return result;
//
//            NestedAggregate bestSpecsNested = bestSpecsAgg.nested();
//            Aggregate onlyVariantAgg = bestSpecsNested.aggregations().get("only_variant");
//            if (onlyVariantAgg == null || !onlyVariantAgg.isFilter()) return result;
//
//            FilterAggregate onlyVariant = onlyVariantAgg.filter();
//
//            Aggregate byKeyAgg = onlyVariant.aggregations().get("by_key");
//            if (byKeyAgg == null || !byKeyAgg.isSterms()) return result;
//
//            StringTermsAggregate byKey = byKeyAgg.sterms();
//
//            for (StringTermsBucket keyBucket : byKey.buckets().array()) {
//                String key = keyBucket.key().stringValue();
//
//                Aggregate byValueAgg = keyBucket.aggregations().get("by_value");
//                if (byValueAgg == null || !byValueAgg.isSterms()) continue;
//
//                StringTermsAggregate byValue = byValueAgg.sterms();
//                Map<String, Long> valueMap = new HashMap<>();
//
//                for (StringTermsBucket valueBucket : byValue.buckets().array()) {
//                    valueMap.put(
//                            valueBucket.key().stringValue(),
//                            valueBucket.docCount()
//                    );
//                }
//
//                result.put(key, valueMap);
//            }
//
//            return result;
//        }

//        public Map<String, Map<String, Long>> mergeAggMaps(
//                Map<String, Map<String, Long>> techMap,
//                Map<String, Map<String, Long>> variantMap) {
//
//            Map<String, Map<String, Long>> result = new HashMap<>();
//            techMap.forEach((key, valueMap) ->
//                    result.put(key, new HashMap<>(valueMap))
//            );
//            variantMap.forEach((key, valueMap) -> {
//
//                Map<String, Long> targetValueMap =
//                        result.computeIfAbsent(key, k -> new HashMap<>());
//
//                valueMap.forEach((value, count) ->
//                        targetValueMap.merge(value, count, Long::sum)
//                );
//            });
//
//            return result;
//        }

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
        private void buildSpecAggregations(NativeQueryBuilder builder) {
            Aggregation techAgg = Aggregation.of(a -> a
                    .nested(n -> n.path("specifications"))
                    .aggregations("only_tech", Aggregation.of(a2 -> a2
                            .filter(f -> f
                                    .term(t -> t
                                            .field("specifications.type")
                                            .value("TECH")
                                    )
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

            Aggregation variantAgg = Aggregation.of(a -> a
                    .nested(n -> n.path("productVariants"))
                    .aggregations("best_specs", Aggregation.of(a2 -> a2
                            .nested(n2 -> n2
                                    .path("productVariants.bestSpecifications")
                            )
                            .aggregations("only_variant", Aggregation.of(a3 -> a3
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("productVariants.bestSpecifications.type")
                                                    .value("VARIANT")
                                            )
                                    )
                                    .aggregations("by_key", Aggregation.of(a4 -> a4
                                            .terms(t2 -> t2
                                                    .field("productVariants.bestSpecifications.key")
                                                    .size(50)
                                            )
                                            .aggregations("by_value", Aggregation.of(a5 -> a5
                                                    .terms(t3 -> t3
                                                            .field("productVariants.bestSpecifications.value")
                                                            .size(50)
                                                    )
                                            ))
                                    ))
                            ))
                    ))
            );
            builder.withAggregation("variant_specs", variantAgg);
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
                                    .fields("name.autocomplete",
                                            "name.autocomplete._2gram",
                                            "name.autocomplete._3gram")
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
                                            "name.autocomplete",
                                            "name.autocomplete._2gram",
                                            "name.autocomplete._3gram"
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
    }
