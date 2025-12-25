    package com.example.search_service.service;

    import co.elastic.clients.elasticsearch._types.FieldValue;
    import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
    import org.springframework.data.elasticsearch.core.AggregationsContainer;
    import org.springframework.data.elasticsearch.core.AggregationsContainer.*;
    import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
    import org.springframework.data.elasticsearch.core.aggregation.Aggregation;
    import org.springframework.data.elasticsearch.core.aggregation.AggregationContainer;
    import org.springframework.data.elasticsearch.core.aggregation.ParsedNested;
    import org.springframework.data.elasticsearch.core.aggregation.ParsedFilter;
    import org.springframework.data.elasticsearch.core.aggregation.ParsedStringTerms;
    import org.springframework.data.elasticsearch.core.aggregation.Terms;

    import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
    import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
    import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
    import com.example.search_service.constant.SortType;
    import com.example.search_service.model.Category;
    import com.example.search_service.model.Products;
    import com.example.search_service.viewmodel.*;
    import com.example.search_service.viewmodel.dto.SpecificationFilterDTO;
    import com.example.search_service.viewmodel.dto.request.AdminSearchRequest;
    import com.okits02.common_lib.dto.PageResponse;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
    import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
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
                return b;
            }));

            switch (sortType)
            {
                case DEFAULT -> {
                    break;
                }
                case PRICE_ASC -> {
                    nativeQueryBuilder.withSort(Sort.by(Sort.Direction.ASC, "sellPrice"));
                    break;
                }
                case PRICE_DESC -> {
                    nativeQueryBuilder.withSort(Sort.by(Sort.Direction.DESC, "sellPrice"));
                    break;
                }
                case RATING_ASC -> {
                    nativeQueryBuilder.withSort(Sort.by(Sort.Direction.ASC, "avgRating"));
                    break;
                }
            }
            nativeQueryBuilder.withPageable(PageRequest.of(page, size));
            SearchHits<Products> productsSearchHits = elasticsearchOperations.search(nativeQueryBuilder.build(), Products.class);
            SearchPage<Products> productsSearchPage = SearchHitSupport.searchPageFor(
                    productsSearchHits, nativeQueryBuilder.getPageable());
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
                    .aggregations(getAggregations(productsSearchHits))
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
                if ("tech".equals(attr.getType())) {
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
                if ("variant".equals(attr.getType())) {
                    b.must(m -> m.nested(n1 -> n1
                            .path("productVariants")
                            .query(q1 -> q1.nested(n2 -> n2
                                    .path("productVariants.bestSpecifications")
                                    .query(q2 -> q2.bool(bl -> bl
                                            .must(ms -> ms.term(t -> t
                                                    .field("productVariants.bestSpecifications.key")
                                                    .value(attr.getKey())
                                            ))
                                            .must(ms -> ms.term(t -> t
                                                    .field("productVariants.bestSpecifications.value")
                                                    .value(attr.getValue())
                                            ))
                                            .must(ms -> ms.term(t -> t
                                                    .field("productVariants.bestSpecifications.type")
                                                    .value("variant")
                                            ))
                                    ))
                            ))
                    ));
                }
            }
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

        private void buildSpecAggregations(NativeQueryBuilder builder) {
            Aggregation techAgg = Aggregation.of(a -> a
                    .nested(n -> n.path("specifications"))
                    .aggregations("only_tech", Aggregation.of(a2 -> a2
                            .filter(f -> f
                                    .term(t -> t
                                            .field("specifications.type")
                                            .value("tech")
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
                                                    .value("variant")
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

        private Map<String, Map<String, Long>> parseTechSpecAgg(SearchHits<Products> hits) {

            Map<String, Map<String, Long>> result = new HashMap<>();

            if (!hits.hasAggregations()) return result;

            ElasticsearchAggregations aggregations =
                    (ElasticsearchAggregations) hits.getAggregations();

            ParsedNested techNested =
                    aggregations.get("tech_specs");

            if (techNested == null) return result;

            ParsedFilter onlyTech =
                    techNested.getAggregations().get("only_tech");

            ParsedStringTerms byKey =
                    onlyTech.getAggregations().get("by_key");

            for (Terms.Bucket keyBucket : byKey.getBuckets()) {

                String key = keyBucket.getKeyAsString();

                ParsedStringTerms byValue =
                        keyBucket.getAggregations().get("by_value");

                Map<String, Long> valueMap = new HashMap<>();
                for (Terms.Bucket v : byValue.getBuckets()) {
                    valueMap.put(v.getKeyAsString(), v.getDocCount());
                }

                result.put(key, valueMap);
            }

            return result;
        }
        private Map<String, Map<String, Long>> getAggregations(SearchHits<Products> searchHits)
        {
            List<org.springframework.data.elasticsearch.client.elc.Aggregation> aggregations = new ArrayList<>();
            if(searchHits.hasAggregations())
            {
                ((List<ElasticsearchAggregation>)searchHits.getAggregations().aggregations())
                        .forEach(elsAgg -> aggregations.add(elsAgg.aggregation()));
            }
            Map<String, Map<String, Long>> aggregationsMap = new HashMap<>();
            aggregations.forEach(aggregation -> {
                Map<String, Long> agg = new HashMap<>();
                StringTermsAggregate stringTermsAggregate = (StringTermsAggregate) aggregation.getAggregate()._get();
                List<StringTermsBucket> stringTermsBuckets =
                        (List<StringTermsBucket>) stringTermsAggregate.buckets()._get();
                stringTermsBuckets.forEach(bucket -> agg.put(bucket.key()._get().toString(), bucket.docCount()));
                aggregationsMap.put(aggregation.getName(), agg);
                });
            return aggregationsMap;
        }

        public ProductNameGetListVm autoCompleteProductName(final String keyword){
            NativeQuery nativeQuery = NativeQuery.builder().withQuery(q -> q
                    .matchPhrasePrefix(m -> m.field("name").query(keyword)))
                    .withSourceFilter(new FetchSourceFilter(
                            true, new String[]{"name"}, null
                    )).build();
            SearchHits<Products> result = elasticsearchOperations.search(nativeQuery, Products.class);
            List<Products> products = result.stream().map(SearchHit::getContent).toList();
            return new ProductNameGetListVm(products.stream().map(ProductNameGetVm::fromModel).toList());
        }

        public CategoryGetListVM autocompleteCategory(String prefix, int size, int page) {
            Pageable pageable = PageRequest.of(page, size);

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .matchPhrasePrefix(m -> m
                                    .field("name")
                                    .query(prefix)
                            )
                    )
                    .withPageable(pageable)
                    .build();

            SearchHits<Category> hits =
                    elasticsearchOperations.search(query, Category.class);

            SearchPage<Category> searchPage =
                    SearchHitSupport.searchPageFor(hits, pageable);

            List<CategoryGetVM> categoryGetVMS = hits.stream()
                    .map(hit -> CategoryGetVM.fromEntity(hit.getContent()))
                    .toList();

            return CategoryGetListVM.builder()
                    .CategoryGetVM(categoryGetVMS)
                    .totalElements(searchPage.getTotalElements())
                    .totalPage(searchPage.getTotalPages())
                    .build();
        }

    }
