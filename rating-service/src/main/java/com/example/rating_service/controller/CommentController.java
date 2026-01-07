package com.example.rating_service.controller;

import com.example.rating_service.dto.request.CommentCreationRequest;
import com.example.rating_service.dto.request.CommentUpdateRequest;
import com.example.rating_service.dto.response.CommentResponse;
import com.example.rating_service.services.CommentService;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/create")
    public ApiResponse<CommentResponse> save(
            @RequestBody CommentCreationRequest request
            ){
        return ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("creation comment successfully!")
                .result(commentService.save(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<CommentResponse> update(
            @RequestBody CommentUpdateRequest request
            ){
        return ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("modify comment successfully")
                .result(commentService.update(request))
                .build();
    }

    @GetMapping("/get/product")
    public ApiResponse<PageResponse<CommentResponse>> getAllForProduct(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "productId") String productId
    ){
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .code(200)
                .message("get all comment successfully")
                .result(commentService.getAllForProduct(page - 1, size, productId))
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<?>  deleteById(
            @RequestParam(value = "id" ) String id
    ){
        commentService.delete(id);
        return ApiResponse.builder()
                .code(200)
                .message("delete by id successfully!")
                .build();
    }


    @DeleteMapping("/delete/product")
    public ApiResponse<?>  deleteByProductId(
            @RequestParam(value = "ProductId" ) String productId
    ){
        commentService.deleteForProductId(productId);
        return ApiResponse.builder()
                .code(200)
                .message("delete by id successfully!")
                .build();
    }
}
