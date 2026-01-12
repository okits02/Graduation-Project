package com.example.search_service.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.GetResponse;
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
import org.springframework.util.StringUtils;

import java.io.IOException;
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
        if (StringUtils.hasText(request.getParentId())) {
            updateParentCategory(request.getParentId(), request.getId());
        }
    }

    public void updateCategory(CategoryEventDTO request) throws IOException {
        Map<String, Object> updateFields = new HashMap<>();

        if (request.getName() != null) {
            updateFields.put("name", request.getName());
        }
        if (request.getDescriptions() != null) {
            updateFields.put("description", request.getDescriptions());
        }
        if (request.getParentId() != null) {
            updateFields.put("parentId", request.getParentId());
        }
        if (request.getChildrentId() != null){
            updateFields.put("childrenId", request.getChildrentId());
        }

        elasticsearchClient.update(
                u -> u
                        .index("category")
                        .id(request.getId())
                        .doc(updateFields),
                Category.class
        );
    }

    public void deleteCategory(String categoryId) throws IOException {
        GetResponse<Category> getResponse = elasticsearchClient.get(
                g -> g.index("category").id(categoryId),
                Category.class
        );

        if (!getResponse.found() || getResponse.source() == null) {
            log.warn("Category not found in ES: {}", categoryId);
            return;
        }
        Category category = getResponse.source();
        if (category.getParentId() != null && !category.getParentId().isBlank()) {

            GetResponse<Category> getParent = elasticsearchClient.get(
                    g -> g.index("category").id(category.getParentId()),
                    Category.class
            );

            if (getParent.found() && getParent.source() != null) {

                List<String> childrenIds =
                        getParent.source().getChildrenId() != null
                                ? new ArrayList<>(getParent.source().getChildrenId())
                                : new ArrayList<>();

                if (childrenIds.remove(categoryId)) {
                    elasticsearchClient.update(
                            u -> u
                                    .index("category")
                                    .id(category.getParentId())
                                    .doc(Map.of("childrenId", childrenIds)),
                            Category.class
                    );
                }
            }
        }
        List<String> idsToDelete = new ArrayList<>();
        collectAllDescendantIds(category, idsToDelete);
        idsToDelete.add(categoryId);

        elasticsearchClient.deleteByQuery(d -> d
                .index("category")
                .query(q -> q
                        .ids(i -> i.values(idsToDelete))
                )
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
            boolean exists = elasticsearchClient.exists(e -> e
                    .index("category")
                    .id(request.getOwnerId())
            ).value();

            if (!exists) {
                throw new AppException(SearchErrorCode.CATEGORY_NOT_EXISTS);
            }

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

    public CategoryDetailsVM getCategoryTreeById(String categoryId) {

        Category root = findById(categoryId);
        return buildTreeByChildrenId(root, new HashSet<>());
    }

    private Category findById(String id) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("_id")
                                .value(id)
                        )
                )
                .build();

        SearchHits<Category> hits =
                elasticsearchOperations.search(query, Category.class);

        if (hits.isEmpty()) {
            throw new RuntimeException("Category not found: " + id);
        }

        return hits.getSearchHit(0).getContent();
    }
    private CategoryDetailsVM buildTreeByChildrenId(
            Category category,
            Set<String> visited
    ) {

        if (!visited.add(category.getId())) {
            return null;
        }

        List<CategoryDetailsVM> childrenVM = new ArrayList<>();

        if (category.getChildrenId() != null && !category.getChildrenId().isEmpty()) {
            NativeQuery childrenQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .terms(t -> t
                                    .field("id")
                                    .terms(v -> v.value(
                                            category.getChildrenId()
                                                    .stream()
                                                    .map(FieldValue::of)
                                                    .toList()
                                    ))
                            )
                    )
                    .withSort(s -> s
                            .field(f -> f
                                    .field("name.keyword")
                                    .order(SortOrder.Asc)
                            )
                    )
                    .withMaxResults(100)
                    .build();

            SearchHits<Category> childrenHits =
                    elasticsearchOperations.search(childrenQuery, Category.class);

            for (SearchHit<Category> hit : childrenHits) {
                CategoryDetailsVM child =
                        buildTreeByChildrenId(hit.getContent(), visited);
                if (child != null) {
                    childrenVM.add(child);
                }
            }
        }

        return CategoryDetailsVM.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .childrenId(childrenVM)
                .build();
    }

    public void updateParentCategory(String parentId, String categoryId) throws IOException {
        GetResponse<Category> getResponse = elasticsearchClient.get(
                g -> g
                        .index("category")
                        .id(parentId),
                Category.class
        );

        if (!getResponse.found() || getResponse.source() == null) {
            log.warn("⚠️ Parent category not found: {}", parentId);
            return;
        }

        Category parent = getResponse.source();
        List<String> childrenIds =
                parent.getChildrenId() != null
                        ? new ArrayList<>(parent.getChildrenId())
                        : new ArrayList<>();
        if (!childrenIds.contains(categoryId)) {
            childrenIds.add(categoryId);
        }
        elasticsearchClient.update(
                u -> u
                        .index("category")
                        .id(parentId)
                        .doc(Map.of("childrenId", childrenIds)),
                Category.class
        );
    }

    private void collectAllDescendantIds(Category category, List<String> result) {

        if (category.getChildrenId() == null || category.getChildrenId().isEmpty()) {
            return;
        }

        for (String childId : category.getChildrenId()) {

            result.add(childId);

            try {
                GetResponse<Category> childResponse = elasticsearchClient.get(
                        g -> g.index("category").id(childId),
                        Category.class
                );

                if (childResponse.found() && childResponse.source() != null) {
                    collectAllDescendantIds(childResponse.source(), result);
                }

            } catch (IOException e) {
                log.error("Failed to load child category {}", childId, e);
            }
        }
    }
}
