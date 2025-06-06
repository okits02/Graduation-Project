package com.example.search_service.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.json.JsonData;
import com.example.search_service.constant.SortType;
import com.example.search_service.model.Products;
import com.example.search_service.viewmodel.ProductGetListVM;
import com.example.search_service.viewmodel.ProductGetVM;
import com.example.search_service.viewmodel.ProductNameGetListVm;
import com.example.search_service.viewmodel.ProductNameGetVm;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductGetListVM searchProductAdvance(String keyword,
                                                 Integer page,
                                                 Integer size,
                                                 List<String> category,
                                                 List<Map<String, String>> attributes,
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
            extractCategory(category, "categories", b);
            extractAttributes(attributes, "specifications", b);
            extractRange(minPrice, maxPrice, "sellPrice", b);
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
        List<ProductGetVM> productGetVMList = productsSearchHits.stream().map(i -> ProductGetVM
                .fromEntity(i.getContent())).toList();
        return ProductGetListVM.builder()
                .productGetVMList(productGetVMList)
                .currentPages(productsSearchPage.getNumber())
                .totalPage(productsSearchPage.getTotalPages())
                .pageSize(productsSearchPage.getSize())
                .totalElements(productsSearchPage.getTotalElements())
                .aggregations(getAggregations(productsSearchHits))
                .build();
    }

    private void extractAttributes(List<Map<String, String>> attributes, String productField, BoolQuery.Builder b) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        for (Map<String, String> attr : attributes)
        {
            b.must(m -> m
                    .nested(nested -> nested
                            .path(productField)
                            .query(q -> q
                                    .bool(bl -> bl
                                            .must(must1 -> must1
                                                    .term(t1 -> t1
                                                            .field(productField + ".key")
                                                            .value(attr.get("key"))
                                                    )
                                            )
                                            .must(must2 -> must2
                                                    .term(t2 -> t2
                                                            .field(productField + ".value")
                                                            .value(attr.get("value"))
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }
    }

    private void extractCategory(List<String> category, String productField, BoolQuery.Builder b)
    {
        if(category == null || category.isEmpty())
        {
            return;
        }

        for(String cate : category)
        {
            b.must(m -> m
                    .nested(nested -> nested
                            .path(productField)
                            .query(q -> q.bool(bl -> bl
                                    .must(m1 -> m1
                                            .term(t -> t
                                                    .field("categories.name")
                                                    .value(cate)
                                            )
                                    )
                                    )
                            )
                    )
            );
        }
    }

    private void extractRange(Number min, Number max, String productField, BoolQuery.Builder b)
    {
        if(min != null || max != null)
        {
            b.must(m -> m.range(r -> r
                    .field(productField)
                    .gte(min != null ? JsonData.fromJson(min.toString()) : null)
                    .lte(max != null ? JsonData.fromJson(max.toString()) : null)
            ));
        }
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
                        new String[]{"name"}, null
                )).build();
        SearchHits<Products> result = elasticsearchOperations.search(nativeQuery, Products.class);
        List<Products> products = result.stream().map(SearchHit::getContent).toList();
        return new ProductNameGetListVm(products.stream().map(ProductNameGetVm::fromModel).toList());
    }
}
