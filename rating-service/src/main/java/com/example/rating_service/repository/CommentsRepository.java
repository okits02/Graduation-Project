package com.example.rating_service.repository;

import com.example.rating_service.model.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;


public interface CommentsRepository extends JpaRepository<Comments, String> {
    Page<Comments> findAllByProductIdAndParentIdIsNull(
            String productId,
            Pageable pageable
    );

    List<Comments> findAllByParentId(String parentId);

    List<Comments> findAllByParentIdIn(List<String> parentIds);
    void deleteAllByProductId(String productId);
}
