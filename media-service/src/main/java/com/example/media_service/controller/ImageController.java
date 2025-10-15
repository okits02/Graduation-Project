package com.example.media_service.controller;

import com.okits02.common_lib.dto.ApiResponse;
import com.example.media_service.dto.request.*;
import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.dto.response.ProductImageResponse;
import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.kafka.ApplyThumbnailEvent;
import com.example.media_service.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Operation(summary = "admin upload product image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/product/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListMediaResponse>> uploadImage(
            @ModelAttribute ImageProductPostRequest request) throws IOException {
        ListMediaResponse listMediaResponse = imageService
                .imageProduct(request.getThumbnail(), request.getImageProducts(), request.getProductId());
        MediaResponse mediaResponse =
                listMediaResponse.getMediaResponseList().stream()
                        .filter(m -> m.getMediaPurpose() == MediaPurpose.THUMBNAIL)
                        .findFirst()
                        .orElse(null);


        if(mediaResponse != null) {
            ApplyThumbnailEvent applyThumbnailEvent = createEvent(mediaResponse.getOwnerId(), mediaResponse.getUrl());
            kafkaTemplate.send("product-apply-thumbnail-event", applyThumbnailEvent).whenComplete(
                    (result, ex) -> {
                        if(ex != null)
                        {
                            System.err.println("Failed to send message" + ex.getMessage());
                        }else {
                            System.err.println("send message successfully" + result.getProducerRecord());
                        }
            });
        }
        return ResponseEntity.ok(ApiResponse.<ListMediaResponse>builder()
                .code(200)
                .message("upload file successfully")
                .result(listMediaResponse)
                .build());
    }
    @PostMapping("/product/change-thumbnail")
    public ResponseEntity<ApiResponse<MediaResponse>> changeThumbnail(
            @ModelAttribute ChangeThumbnailRequest request
    ) throws IOException {
        MediaResponse mediaResponse = imageService.changeThumbnail(request.getOldThumbnailUrl(),
                request.getNewThumbnail(), request.getProductId());
        ApplyThumbnailEvent applyThumbnailEvent = createEvent(mediaResponse.getOwnerId(), mediaResponse.getUrl());
        kafkaTemplate.send("product-apply-thumbnail-event", applyThumbnailEvent).whenComplete(
                (result, ex) -> {
                    if(ex != null)
                    {
                        System.err.println("Failed to send message" + ex.getMessage());
                    }else {
                        System.err.println("send message successfully" + result.getProducerRecord());
                    }
                });
        return ResponseEntity.ok(ApiResponse.<MediaResponse>builder()
                .code(200)
                .message("Chang photo thumbnail successfully")
                .result(mediaResponse)
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

    @DeleteMapping("/delete/ownerId")
    public ResponseEntity<ApiResponse<?>> deleteByOwnerId(
            @RequestBody DeleteMediaRequest request
            ){
        imageService.deleteByOwnerId(request.getOwnerId(), request.getMediaOwnerType());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Delete media successfully")
                .build());
    }

    @DeleteMapping("/delete/url")
    public ResponseEntity<ApiResponse<?>> deleteUrl(
        @RequestBody DeleteMediaByUrlRequest request
    ){
        imageService.deleteByUrl(request.getUrl());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Delete media successfully")
                .build());
    }

    @PutMapping("/{mediaId}/reorder")
    public ResponseEntity<Void> reorderImage(
            @PathVariable String mediaId,
            @RequestParam int newPosition) {
        imageService.changePosition(mediaId, newPosition);
        return ResponseEntity.ok().build();
    }


    private ApplyThumbnailEvent createEvent(String productId, String url){
        return ApplyThumbnailEvent.builder()
                .productId(productId)
                .url(url)
                .build();
    }
}
