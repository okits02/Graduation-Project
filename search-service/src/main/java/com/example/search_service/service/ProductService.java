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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.springframework.data.domain.PageRequest;
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
                                                 String category)
    {
        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
                .withAggregation("categories", Aggregation.of(
                        a -> a.terms(ta-> ta.field("categories"))))
                .withAggregation("specifications", Aggregation.of(
                        a -> a.terms(ta -> ta.field("specification"))))
                .withPageable(PageRequest.of(page, size));
        if(keyword != null)
        {
            nativeQueryBuilder.withQuery(q -> q
                    .matchPhrasePrefix(m -> m
                            .field("name")
                            .query(keyword)));
        }else {
            nativeQueryBuilder.withQuery(q -> q.matchAll(ma -> ma));
        }
        nativeQueryBuilder.withFilter(f -> f.bool(
                b -> {
                    if(category != null) {
                        extractedStr(category, "categories", b);
                    }
                    return b;
                }
        ));
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

    private void extractedStr(String strField, String productField, BoolQuery.Builder b)
    {
        if(strField != null && !strField.isBlank())
        {
            String[] strFields = strField.split(",");
            for(String str : strFields)
            {
                b.should(s -> s.term(t -> t
                        .field(productField)
                        .value(str)
                        .caseInsensitive(true)));
            }
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
