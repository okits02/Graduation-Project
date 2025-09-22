package com.example.media_service.controller;

import com.example.media_service.dto.ApiResponse;
import com.example.media_service.dto.request.*;
import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.dto.response.ProductImageResponse;
import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @Operation(summary = "admin upload product image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/product/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListMediaResponse>> uploadImage(
            @ModelAttribute ImageProductPostRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.<ListMediaResponse>builder()
                .code(200)
                .message("upload file successfully")
                .result(imageService.imageProduct(request.getThumbnail(), request.getImageProducts(), request.getProductId()))
                .build());
    }

    @PostMapping("/product/video")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadVide(
            @RequestBody VideoProductPostRequest request
            ) throws IOException {
        return ResponseEntity.ok(ApiResponse.<MediaResponse>builder()
                .code(200)
                .message("create video successfully")
                .result(imageService.videoProduct(request.getVideoProduct(), request.getProductId()))
                .build());
    }

    @PostMapping("/category/media")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadCateImage(
            @ModelAttribute ImageUploadRequest request
            ) throws IOException {
        return ResponseEntity.ok(ApiResponse.<MediaResponse>builder()
                        .code(200)
                        .message("create image successfully")
                        .result(imageService.imageCategory(request.getMultipartFile(), request.getOwnerId()))
                .build());
    }

    @GetMapping("/product/get-media")
    public ResponseEntity<ApiResponse<ListMediaResponse>> getMedia(
            @RequestParam("ownerId") String ownerId,
            @RequestParam("mediaOwnerType") MediaOwnerType mediaOwnerType
            ){
        return ResponseEntity.ok(ApiResponse.<ListMediaResponse>builder()
                .code(200)
                .message("get media successfully")
                .result(imageService.getMedia(ownerId, mediaOwnerType))
                .build());
    }

    @DeleteMapping("/delete-by-ownerI")
    public ResponseEntity<ApiResponse<?>> deleteByOwnerId(
            @RequestBody DeleteMediaRequest request
            ){
        imageService.deleteByOwnerId(request.getOwnerId(), request.getMediaOwnerType());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Delete media successfully")
                .build());
    }

    @DeleteMapping("/delete-by-url")
    public ResponseEntity<ApiResponse<?>> deleteUrl(
        @RequestBody DeleteMediaByUrlRequest request
    ){
        imageService.deleteByUrl(request.getUrl());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Delete media successfully")
                .build());
    }

}
