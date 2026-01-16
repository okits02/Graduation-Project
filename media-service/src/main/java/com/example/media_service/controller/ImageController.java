package com.example.media_service.controller;

import com.example.media_service.dto.response.BannerResponse;
import com.example.media_service.dto.response.EnumsResponse;
import com.example.media_service.enums.MediaType;
import com.example.media_service.model.Media;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.media_service.dto.request.*;
import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.kafka.ApplyThumbnailEvent;
import com.example.media_service.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
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
                .imageProduct(request.getImageProducts(), request.getProductId());
        MediaResponse mediaResponse =
                listMediaResponse.getMediaResponseList().stream()
                        .filter(m -> m.getMediaPurpose() == MediaPurpose.THUMBNAIL)
                        .findFirst()
                        .orElse(null);
        return ResponseEntity.ok(ApiResponse.<ListMediaResponse>builder()
                .code(200)
                .message("upload file successfully")
                .result(listMediaResponse)
                .build());
    }
    @PostMapping("/product/change-thumbnail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MediaResponse>> changeThumbnail(
            @ModelAttribute ChangeThumbnailRequest request
    ) throws IOException {

        MediaResponse mediaResponse = imageService.changeThumbnail(request.getOldThumbnailUrl(),
                request.getNewThumbnail(), request.getSku());
        ApplyThumbnailEvent applyThumbnailEvent = createEvent(
                mediaResponse.getOwnerId(), request.getProductId(), mediaResponse.getUrl(), mediaResponse.getOwnerType()
        );
        kafkaTemplate.send("apply-thumbnail-event", applyThumbnailEvent).whenComplete(
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

    @PostMapping("/product/change-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListMediaResponse>> changeImage(
            @ModelAttribute ChangeImageProductRequest request
    ) throws IOException {
        ListMediaResponse listMediaResponse = imageService.changeImageProduct(request.getListFile(),
                request.getProductId());
        return ResponseEntity.ok(ApiResponse.<ListMediaResponse>builder()
                        .code(200)
                        .message("Create new image successfully!")
                        .result(listMediaResponse)
                .build());
    }

    @PostMapping("/banner")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> creationBanner(
            @ModelAttribute BannerCreationRequest request
    ) throws IOException {
        return ApiResponse.<BannerResponse>builder()
                .code(200)
                .message("creation banner successfully")
                .result(imageService.createBanner(request))
                .build();
    }


    @PostMapping("/thumbnail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadImage(
            @ModelAttribute ImageUploadRequest request
            ) throws IOException {
        MediaResponse mediaResponse = imageService.uploadImage(request.getMultipartFile(),
                request.getOwnerId(), request.getMediaOwnerType());
        ApplyThumbnailEvent applyThumbnailEvent = createEvent(
                mediaResponse.getOwnerId(), request.getProductId(), mediaResponse.getUrl(), mediaResponse.getOwnerType()
        );
        kafkaTemplate.send("apply-thumbnail-event", applyThumbnailEvent).whenComplete(
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
                        .message("create image successfully")
                        .result(mediaResponse)
                .build());
    }

    @PostMapping("/user/avatar")
    public ApiResponse<MediaResponse> creationAvatar(
            @ModelAttribute AvatarUserCreationRequest request
    ) throws IOException {
        return ApiResponse.<MediaResponse>builder()
                .code(200)
                .message("creation avatar for user successfully")
                .result(imageService.createAvatarUser(request))
                .build();
    }

    @PostMapping("/rating/image")
    public ApiResponse<?> creationImageForRating(
        @ModelAttribute ImageRatingRequest request
    ) throws IOException {
        imageService.updateImageForRating(request.getMultipartFileList(), request.getRatingId());
        return ApiResponse.builder()
                .code(200)
                .message("creating image for rating successfully")
                .build();
    }

    @PostMapping("/comment/image")
    public ApiResponse<?> creationImageForComment(
            @ModelAttribute ImageCommentRequest request
    ) throws IOException {
        imageService.updateImageForComment(request.getMultipartFileList(), request.getCommentId());
        return ApiResponse.builder()
                .code(200)
                .message("creating image for rating successfully")
                .build();
    }

    @GetMapping("/banner/get")
    public ApiResponse<List<BannerResponse>> getAllBanner(){
        return ApiResponse.<List<BannerResponse>>builder()
                .code(200)
                .message("creation banner successfully")
                .result(imageService.getAllBanner())
                .build();
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

    @GetMapping("/media-owner-types")
    public List<EnumsResponse> mediaOwnerTypes() {
        return Arrays.stream(MediaOwnerType.values())
                .map(e -> new EnumsResponse(e.getCode(), e.getLabel()))
                .toList();
    }

    @GetMapping("/media-purposes")
    public List<EnumsResponse> mediaPurposes() {
        return Arrays.stream(MediaPurpose.values())
                .map(e -> new EnumsResponse(e.name(), e.name()))
                .toList();
    }

    @GetMapping("/media-types")
    public List<EnumsResponse> mediaTypes() {
        return Arrays.stream(MediaType.values())
                .map(e -> new EnumsResponse(e.name(), e.name()))
                .toList();
    }

    @DeleteMapping("/delete/ownerId")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUrl(
        @RequestBody DeleteMediaByUrlRequest request
    ){
        imageService.deleteByUrl(request.getUrl());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Delete media successfully")
                .build());
    }

    @PutMapping("/product/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> reorderImage(
            @RequestBody ReorderImageRequest request
            ) {
        imageService.changePosition(request.getImageId(), request.getNewPosition());
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("reorder position successfully !")
                .build());
    }


    private ApplyThumbnailEvent createEvent(String ownerId, String productId,
                                            String url, MediaOwnerType mediaOwnerType){
        ApplyThumbnailEvent applyThumbnailEvent = ApplyThumbnailEvent.builder()
                .ownerId(ownerId)
                .mediaOwnerType(String.valueOf(mediaOwnerType))
                .url(url)
                .build();
        if(productId != null){
            applyThumbnailEvent.setProductId(productId);
        }
        return applyThumbnailEvent;
    }
}
