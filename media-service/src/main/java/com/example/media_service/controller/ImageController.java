package com.example.media_service.controller;

import com.example.media_service.dto.ApiResponse;
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
            @RequestPart("thumbnail") MultipartFile thumbNailFile,
            @RequestPart("imageProduct") List<MultipartFile> multipartFile,
            @RequestPart("name") String name) throws IOException {
        ProductImageResponse imageUrl = imageService.imageProduct(thumbNailFile, multipartFile, name);
        return ResponseEntity.ok(ApiResponse.<ProductImageResponse>builder()
                .code(200)
                .message("upload file successfully")
                .result(imageUrl)
                .build());
    }

    @Operation(summary = "admin upload category image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadCategoryImage(
            @RequestPart("imageCategory") MultipartFile multipartFile,
            @RequestPart("name")  String name) throws IOException {
        String imageUrl = imageService.url(multipartFile, name);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("upload image successfully")
                .result(imageUrl)
                .build());
    }

    @Operation(summary = "admin delete image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            imageService.deleteImage(imageUrl);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Delete failed: " + e.getMessage());
        }
    }
}
