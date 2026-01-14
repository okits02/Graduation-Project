package com.example.rating_service.helper;

import com.example.rating_service.dto.CustomerVM;
import com.example.rating_service.dto.response.CommentResponse;
import com.example.rating_service.exception.RatingErrorCode;
import com.example.rating_service.mapper.CommentMapper;
import com.example.rating_service.model.Comments;
import com.example.rating_service.repository.httpClient.ProfileClient;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapperHelper {
    private final CommentMapper commentMapper;

    public CommentResponse toResponse(Comments comments, CustomerVM customerVM){
        CommentResponse commentResponse = commentMapper.toResponse(comments);
        commentResponse.setFirstName(customerVM.getFirstName());
        commentResponse.setLastName(customerVM.getLastName());
        commentResponse.setAvatarUrl( customerVM.getAvatarUrl());
        return commentResponse;
    }}
