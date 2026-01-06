package com.example.rating_service.repository;

import com.example.rating_service.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface CommentsRepository extends JpaRepository<Comments, String> {
}
