package com.example.media_service.service;

import com.example.media_service.dto.request.AvatarUserCreationRequest;
import com.example.media_service.dto.request.BannerCreationRequest;
import com.example.media_service.dto.response.BannerResponse;
import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.enums.MediaOwnerType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public ListMediaResponse imageProduct(List<MultipartFile> imageProductFile,
                                          String productId) throws IOException;
    public MediaResponse uploadImage(MultipartFile thumbNailFile, String ownerId, MediaOwnerType mediaOwnerType) throws IOException;
    public MediaResponse changeThumbnail(String oldThumbnailUrl, MultipartFile newThumbnail, String sku) throws IOException;
    public ListMediaResponse changeImageProduct(List<MultipartFile> listFileImage, String productId ) throws IOException;
    public void deleteByOwnerId(String OwnerId, MediaOwnerType mediaOwnerType);
    public ListMediaResponse getMedia(String ownerId, MediaOwnerType mediaOwnerType);
    public List<BannerResponse> createBanner(BannerCreationRequest request) throws IOException;
    public MediaResponse createAvatarUser(AvatarUserCreationRequest request) throws IOException;
    public List<BannerResponse> getAllBanner();
    public void changePosition(String mediaId, Integer newPosition);
    public void deleteByUrl(String url);
}
