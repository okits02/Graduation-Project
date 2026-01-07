package com.example.rating_service.services;

import com.example.rating_service.dto.request.CommentCreationRequest;
import com.example.rating_service.dto.request.CommentUpdateRequest;
import com.example.rating_service.dto.response.CommentResponse;
import com.okits02.common_lib.dto.PageResponse;

public interface CommentService {
    public CommentResponse save(CommentCreationRequest request);
    public CommentResponse update(CommentUpdateRequest request);

    public PageResponse<CommentResponse> getAllForProduct(int page, int size, String productId);
    public void delete(String id);
    public void deleteForProductId(String productId);
}
