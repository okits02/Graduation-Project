package com.example.media_service.controller;

import com.example.media_service.dto.ApiResponse;
import com.example.media_service.dto.request.ImageProductPostRequest;
import com.example.media_service.dto.response.ProductImageResponse;
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
    @PostMapping("/upload-product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
            @RequestBody ImageProductPostRequest request) throws IOException {
        ProductImageResponse imageUrl = imageService.imageProduct(request.getThumbnail(), request.getImageProducts(),
                request.getProductId());
        return ResponseEntity.ok(ApiResponse.<ProductImageResponse>builder()
                .code(200)
                .message("upload file successfully")
                .result(imageUrl)
                .build());
    }

}
