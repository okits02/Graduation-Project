package com.example.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.search_service.exceptions.SearchErrorCode;
import com.example.search_service.model.Category;
import com.example.search_service.viewmodel.CategoryDetailsVM;
import com.example.search_service.viewmodel.CategoryGetListVM;
import com.example.search_service.viewmodel.CategoryGetVM;
import com.example.search_service.viewmodel.dto.CategoryEventDTO;
import com.example.search_service.viewmodel.dto.request.ApplyThumbnailRequest;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    public Map<String, CategoryGetVM> getCategoryByIds(Set<String> categoryIds){
        if(categoryIds == null || categoryIds.isEmpty()){
            return Collections.emptyMap();
        }
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .terms(t -> t
                                .field("_id")
                                .terms(v -> v
                                        .value(categoryIds
                                                .stream()
                                                .map(FieldValue::of)
                                                .toList()
                                        )
                                )
                        )
                )
                .withMaxResults(categoryIds.size())
                .build();
        SearchHits<Category> hits = elasticsearchOperations.search(nativeQuery, Category.class);
        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors
                        .toMap(Category::getId, c -> CategoryGetVM.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .description(c.getDescription())
                                .parentId(c.getParentId())
                                .childrenId(c.getChildrenId())
                                .build()
                        )
                );
    }

    public void indexCategory(CategoryEventDTO request) throws IOException {
        log.error("🔥 CATEGORY INDEX HIT: {}", request);
        Category category = Category.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescriptions())
                .parentId(request.getParentId())
                .childrenId(request.getChildrentId())
                .build();
        elasticsearchClient.index(i -> i
                .index("category")
                .id(request.getId())
                .document(category)
        );
    }

    public void updateCategory(CategoryEventDTO request) throws IOException {
        Map<String, Object> updateFields = new HashMap<>();

        if (request.getName() != null) {
            updateFields.put("name", request.getName());
        }
        if (request.getDescriptions() != null) {
            updateFields.put("descriptions", request.getDescriptions());
        }
        if (request.getParentId() != null) {
            updateFields.put("parentId", request.getParentId());
        }


        elasticsearchClient.update(
                u -> u
                        .index("category")
                        .id(request.getId())
                        .doc(updateFields),
                Object.class
        );
    }

    public void deleteCategory(String categoryId) throws IOException {

        elasticsearchClient.delete(d -> d
                .index("category")
                .id(categoryId)
        );
    }
    public void applyThumbnailToCategory(ApplyThumbnailRequest request) {

        if (request == null
                || request.getOwnerId() == null
                || request.getOwnerId().isBlank()
                || request.getUrl() == null
                || request.getUrl().isBlank()) {

            throw new AppException(SearchErrorCode.INVALID_REQUEST);
        }

        try {
            // 1️⃣ Check category exists
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category")
                    .id(request.getOwnerId())
            ).value();

            if (!exists) {
                throw new AppException(SearchErrorCode.CATEGORY_NOT_EXISTS);
            }

            // 2️⃣ Partial update thumbnail
            elasticsearchClient.update(
                    u -> u
                            .index("category")
                            .id(request.getOwnerId())
                            .doc(Map.of(
                                    "imageUrl", request.getUrl()
                            )),
                    Object.class
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CategoryGetListVM getCategoryByListIds(List<String> categoryIds){
        if(categoryIds == null || categoryIds.isEmpty()){
            return CategoryGetListVM.builder().build();
        }
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .terms(t -> t
                                .field("_id")
                                .terms(v -> v
                                        .value(categoryIds
                                                .stream()
                                                .map(FieldValue::of)
                                                .toList()
                                        )
                                )
                        )
                )
                .withMaxResults(categoryIds.size())
                .build();
        SearchHits<Category> hits = elasticsearchOperations.search(nativeQuery, Category.class);
        List<CategoryGetVM> categoryGetVMS = hits.stream().map(i ->
                        CategoryGetVM.fromEntity(i.getContent()))
                .toList();
        return CategoryGetListVM.builder()
                .totalPage(1)
                .totalElements((long) categoryIds.size())
                .CategoryGetVM(categoryGetVMS)
                .build();
    }

    public CategoryDetailsVM getCategoryByParentId(String categoryId){
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("_id")
                                .value(categoryId)
                        )
                )
                .build();
        SearchHits<Category> hits = elasticsearchOperations.search(nativeQuery, Category.class);
        Category parent = hits.getSearchHit(0).getContent();

        NativeQuery childrentQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("parent_id")
                                .value(categoryId)))
                .withSort(s -> s
                        .field(f -> f
                                .field("name.keyword")
                                .order(SortOrder.Asc)
                        )
                )
                .withMaxResults(100)
                .build();
        SearchHits<Category> childrentHits = elasticsearchOperations.search(nativeQuery, Category.class);
        List<CategoryGetVM> childrentVM = childrentHits.stream()
                .map(i ->
                        CategoryGetVM.fromEntity(i.getContent()))
                .toList();
        return CategoryDetailsVM.builder()
                .id(parent.getId())
                .name(parent.getName())
                .description(parent.getDescription())
                .imageUrl(parent.getImageUrl())
                .parentId(parent.getParentId())
                .childrenId(childrentVM)
                .build();
    }


}
