package com.example.rating_service.mapper;

import com.example.rating_service.dto.request.CommentCreationRequest;
import com.example.rating_service.dto.request.CommentUpdateRequest;
import com.example.rating_service.dto.response.CommentResponse;
import com.example.rating_service.model.Comments;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentResponse toResponse (Comments comments);
    Comments toComments(CommentCreationRequest request);
    void update(@MappingTarget Comments comments, CommentUpdateRequest request);
}
