package com.example.rating_service.services.Impl;

import com.example.rating_service.dto.CustomerVM;
import com.example.rating_service.dto.request.CommentCreationRequest;
import com.example.rating_service.dto.request.CommentUpdateRequest;
import com.example.rating_service.dto.response.CommentResponse;
import com.example.rating_service.dto.response.UserIdResponse;
import com.example.rating_service.exception.RatingErrorCode;
import com.example.rating_service.helper.CommentMapperHelper;
import com.example.rating_service.mapper.CommentMapper;
import com.example.rating_service.model.Comments;
import com.example.rating_service.repository.CommentsRepository;
import com.example.rating_service.repository.httpClient.ProfileClient;
import com.example.rating_service.repository.httpClient.UserClient;
import com.example.rating_service.services.CommentService;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentsRepository commentsRepository;
    private final CommentMapper commentMapper;
    private final CommentMapperHelper commentMapperHelper;
    private final UserClient userClient;
    private final ProfileClient profileClient;
    @Override
    public CommentResponse save(CommentCreationRequest request) {
        UserIdResponse user = getUserId();

        Comments comments = commentMapper.toComments(request);
        comments.setUserId(user.getUserId());
        log.info(
                "[COMMENT][ADMIN_REPLY] userId={}, parentId={}, firstName='{}', lastName='{}'",
                user.getUserId(),
                user.getRole()
        );
        if ("ADMIN".equals(user.getRole())) {

            if (request.getParentId() == null) {
                throw new AppException(RatingErrorCode.ADMIN_ONLY_REPLY_COMMENT);
            }

            comments.setFirstName("");
            comments.setLastName("ADMIN");
            comments.setAvatarUrl(null);
            log.info(
                    "[COMMENT][ADMIN_REPLY] userId={}, parentId={}, firstName='{}', lastName='{}'",
                    user.getUserId(),
                    request.getParentId(),
                    comments.getFirstName(),
                    comments.getLastName()
            );
        }
        else {

            var response = profileClient.getProfileForRating(user.getUserId());

            if (response.getBody() == null || response.getBody().getCode() != 200) {
                throw new AppException(RatingErrorCode.PROFILE_NOT_EXISTS);
            }

            CustomerVM customerVM = response.getBody().getResult();

            comments.setFirstName(customerVM.getFirstName());
            comments.setLastName(customerVM.getLastName());
            comments.setAvatarUrl(customerVM.getAvatarUrl());
        }

        comments.setCreatedAt(LocalDateTime.now());
        commentsRepository.save(comments);
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
        Pageable pageable = PageRequest.of(page, size);

        Page<Comments> rootPage =
                commentsRepository.findAllByProductIdAndParentIdIsNull(productId, pageable);

        List<Comments> roots = rootPage.getContent();

        List<String> rootIds = roots.stream()
                .map(Comments::getId)
                .toList();

        Map<String, List<Comments>> childrenMap = commentsRepository
                .findAllByParentIdIn(rootIds)
                .stream()
                .collect(Collectors.groupingBy(Comments::getParentId));

        List<CommentResponse> data = roots.stream()
                .map(root -> CommentResponse.builder()
                        .id(root.getId())
                        .content(root.getContent())
                        .productId(root.getProductId())
                        .parentId(root.getParentId())
                        .firstName(root.getFirstName())
                        .lastName(root.getLastName())
                        .avatarUrl(root.getAvatarUrl())
                        .imageUrl(root.getImageUrl())
                        .userId(root.getUserId())
                        .createdAt(root.getCreatedAt())
                        .childrent(
                                childrenMap.getOrDefault(root.getId(), List.of())
                                        .stream()
                                        .map(reply -> CommentResponse.builder()
                                                .id(reply.getId())
                                                .content(reply.getContent())
                                                .productId(reply.getProductId())
                                                .parentId(reply.getParentId())
                                                .firstName(reply.getFirstName())
                                                .lastName(reply.getLastName())
                                                .avatarUrl(reply.getAvatarUrl())
                                                .imageUrl(reply.getImageUrl())
                                                .userId(reply.getUserId())
                                                .createdAt(reply.getCreatedAt())
                                                .childrent(List.of())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build()
                )
                .toList();

        return PageResponse.<CommentResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPage(rootPage.getTotalPages())
                .totalElements(rootPage.getTotalElements())
                .data(data)
                .build();
    }


    @Override
    @Transactional
    public void delete(String id) {
        Comments comments = commentsRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(RatingErrorCode.COMMENT_NOT_EXISTS));

        deleteCommentTree(comments.getId());
    }
    @Override
    @Transactional
    public void deleteMyComment(String id) {
        UserIdResponse user = getUserId();

        Comments comments = commentsRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(RatingErrorCode.COMMENT_NOT_EXISTS));

        if (!comments.getUserId().equals(user.getUserId())) {
            throw new AppException(RatingErrorCode.USER_CAN_NOT_DELETE_COMMENT);
        }

        deleteCommentTree(comments.getId());
    }

    @Override
    public void createImageUrl(List<String> imageUrl, String id) {
        Comments comments = commentsRepository.findById(id).orElseThrow(() ->
                new AppException(RatingErrorCode.COMMENT_NOT_EXISTS));

        comments.setImageUrl(imageUrl);
    }

    @Transactional
    public void deleteCommentTree(String commentId){
        Queue<String> queue = new LinkedList<>();
        queue.add(commentId);
        while (!queue.isEmpty()){
            String currentId = queue.poll();
            List<Comments> children = commentsRepository.findAllByParentId(currentId);

            children.forEach(child -> queue.add(child.getParentId()));
            commentsRepository.findById(currentId).ifPresent(c -> {
                c.setIsDeleted(true);
                commentsRepository.save(c);
            });
        }
    }

    @Override
    public void deleteForProductId(String productId) {
        commentsRepository.deleteAllByProductId(productId);
    }
    private UserIdResponse getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult();
    }
}
