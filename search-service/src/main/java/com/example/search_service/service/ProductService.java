package com.example.search_service.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.json.JsonData;
import com.example.search_service.constant.SortType;
import com.example.search_service.model.Products;
import com.example.search_service.viewmodel.ProductGetListVM;
import com.example.search_service.viewmodel.ProductGetVM;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.common.unit.Fuzziness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductGetListVM searchProductAdvance(String keyword,
                                                 Integer page,
                                                 Integer size,
                                                 String category,
                                                 String attribute,
                                                 Double minPrice,
                                                 Double maxPrice,
                                                 SortType sortType)
    {
        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
                .withAggregation("categories", Aggregation.of(
                        a -> a.terms(ta-> ta.field("categories.keyword"))))
                .withAggregation("specifications", Aggregation.of(
                        a -> a.terms(ta -> ta.field("specification.<key>.keyword"))))
                .withQuery(q -> q
                        .bool(b ->  {
                             b.must(m -> m.multiMatch(mm -> mm
                                     .fields("name")
                                     .query(keyword)
                                     .fuzziness(Fuzziness.ONE.asString())));
                             if(category != null && !category.isBlank())
                             {
                                 b.filter(f -> f.term(t -> t
                                         .field("categories.keyword")
                                         .value(category)));
                             }

                             if(attribute != null && !attribute.isBlank())
                             {
                                 b.filter(f -> f.term(t -> t
                                         .field("specifications.keyword")
                                         .value(attribute)));
                             }

                             if(minPrice != null || maxPrice != null)
                             {
                                 b.filter(f -> f.range(r -> {
                                     r.field("sellPrice");
                                     if (minPrice != null) r.gte(JsonData.of(minPrice));
                                     if (maxPrice != null) r.lte(JsonData.of(maxPrice));
                                     return r;
                                 }));
                             }
                             return b;
                        }))
                .withPageable(PageRequest.of(page, size));
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
                .build();
    }

    private void extractedStr(String strField, String productField, BoolQuery.Builder b)
    {
        
    }
}
