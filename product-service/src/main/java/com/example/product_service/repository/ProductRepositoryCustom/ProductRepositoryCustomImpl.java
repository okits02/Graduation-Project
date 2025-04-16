package com.example.product_service.repository.ProductRepositoryCustom;

import com.example.product_service.dto.request.ProductSearchRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.model.Products;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom{
    @PersistenceContext
    private EntityManager entityManager;

    private final ProductMapper productMapper;


    @Override
    public Page<ProductResponse> searchByCriteria(ProductSearchRequest request) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Products> criteriaQuery = criteriaBuilder.createQuery(Products.class);
        Root<Products> root = criteriaQuery.from(Products.class);

        List<Predicate> predicates = new ArrayList<>();

        if(request.getKeyword() != null)
        {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                    "%" + request.getKeyword().toLowerCase() + "%"));
        }

        if(request.getCategoryId() != null)
        {
            predicates.add(criteriaBuilder.equal(root.get("category").get("id"), request.getCategoryId()));
        }

        if(request.getBrand() != null)
        {
            predicates.add(criteriaBuilder.equal(root.get("brand"), request.getBrand()));
        }

        if(request.getPriceFrom() != null)
        {
            predicates.add(criteriaBuilder.equal(root.get("price"), request.getPriceFrom()));
        }

        if(request.getPriceTo() != null)
        {
            predicates.add(criteriaBuilder.equal(root.get("price"), request.getPriceTo()));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        if(request.getSortBy() != null)
        {
            Path<?> sortField = root.get(request.getSortBy());
            criteriaQuery.orderBy("asc".equalsIgnoreCase(request.getSortDirection()) ? criteriaBuilder.asc(sortField)
                    : criteriaBuilder.desc(sortField));
        }

        TypedQuery<Products> query = entityManager.createQuery(criteriaQuery);

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 0;

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<Products> productsList = query.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Products> countRoot = countQuery.from(Products.class);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<ProductResponse> content = productsList.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }
}
