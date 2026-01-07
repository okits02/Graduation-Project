package com.example.rating_service.services.Impl;

import com.example.rating_service.dto.request.CommentCreationRequest;
import com.example.rating_service.dto.request.CommentUpdateRequest;
import com.example.rating_service.dto.response.CommentResponse;
import com.example.rating_service.exception.RatingErrorCode;
import com.example.rating_service.mapper.CommentMapper;
import com.example.rating_service.model.Comments;
import com.example.rating_service.repository.CommentsRepository;
import com.example.rating_service.repository.httpClient.UserClient;
import com.example.rating_service.services.CommentService;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentsRepository commentsRepository;
    private final CommentMapper commentMapper;
    private final UserClient userClient;
    @Override
    public CommentResponse save(CommentCreationRequest request) {
        String userId = getUserId();
        Comments comments = commentMapper.toComments(request);
        comments.setUserId(userId);
        return commentMapper.toResponse(comments);
    }

    @Override
    public CommentResponse update(CommentUpdateRequest request) {
        Comments comments = commentsRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(RatingErrorCode.COMMENT_NOT_EXISTS));
        commentMapper.update(comments, request);
        commentsRepository.save(comments);
        return commentMapper.toResponse(comments);
    }

    @Override
    public PageResponse<CommentResponse> getAllForProduct(int page, int size, String productId) {
        if (page < 1) page = 1;
        if (size <= 0) size = 10;

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Comments> rootComments =
                commentsRepository.findAllByProductIdAndParentIdIsNull(
                        productId,
                        pageable
                );

        List<CommentResponse> data = rootComments.getContent()
                .stream()
                .map(root -> {
                    List<CommentResponse> children =
                            commentsRepository.findAllByParentId(root.getId())
                                    .stream()
                                    .map(reply -> CommentResponse.builder()
                                            .id(reply.getId())
                                            .content(reply.getContent())
                                            .productId(reply.getProductId())
                                            .userId(reply.getUserId())
                                            .parentId(reply.getParentId())
                                            .childrent(List.of())
                                            .build()
                                    )
                                    .toList();

                    return CommentResponse.builder()
                            .id(root.getId())
                            .content(root.getContent())
                            .productId(root.getProductId())
                            .userId(root.getUserId())
                            .parentId(root.getParentId())
                            .childrent(children)
                            .build();
                })
                .toList();

        return PageResponse.<CommentResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPage(rootComments.getTotalPages())
                .totalElements(rootComments.getTotalElements())
                .data(data)
                .build();
    }


    @Override
    public void delete(String id) {
        Comments comments = commentsRepository.findById(id).orElseThrow(() ->
                new AppException(RatingErrorCode.COMMENT_NOT_EXISTS));
        commentsRepository.delete(comments);
    }

    @Override
    public void deleteForProductId(String productId) {
        commentsRepository.deleteAllByProductId(productId);
    }
    private String getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult().getUserId();
    }
}
