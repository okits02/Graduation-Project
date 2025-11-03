package com.example.media_service.service;

import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.dto.response.ProductImageResponse;
import com.example.media_service.enums.MediaOwnerType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public ListMediaResponse imageProduct(MultipartFile thumbNailFile,
                                          List<MultipartFile> imageProductFile,
                                          String productId) throws IOException;
    public MediaResponse imageCategory(MultipartFile thumbNailFile, String categoryId) throws IOException;
    public MediaResponse changeThumbnail(String oldThumbnailUrl, MultipartFile newThumbnail, String productId) throws IOException;
    public ListMediaResponse changeImageProduct(List<MultipartFile> listFileImage, String productId ) throws IOException;
    public void deleteByOwnerId(String OwnerId, MediaOwnerType mediaOwnerType);
    public ListMediaResponse getMedia(String ownerId, MediaOwnerType mediaOwnerType);
    public void changePosition(String mediaId, Integer newPosition);
    public void deleteByUrl(String url);
}
