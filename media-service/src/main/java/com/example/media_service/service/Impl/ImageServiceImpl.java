package com.example.media_service.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import com.okits02.common_lib.exception.AppException;
import com.example.media_service.exception.MediaErrorCode;
import com.example.media_service.mapper.MediaMapper;
import com.example.media_service.model.Media;
import com.example.media_service.repository.MediaRepository;
import com.example.media_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    public ListMediaResponse imageProduct(MultipartFile thumbNailFile,
                                          List<MultipartFile> imageProductFile,
                                          String productId) throws IOException {
        String thumbnailUrl = "";
        List<String> imageProductUrl = new ArrayList<>();
        List<MediaResponse> responses = new ArrayList<>();
        if (thumbNailFile != null) {
            Media thumbnail = uploadAndSave(thumbNailFile, productId, MediaOwnerType.PRODUCT, MediaPurpose.THUMBNAIL);
            responses.add(mediaMapper.toMediaResponse(mediaRepository.save(thumbnail)));
            thumbnailUrl = thumbnail.getUrl();
        }
        for (MultipartFile file : imageProductFile) {
            Media image = uploadAndSave(file, productId, MediaOwnerType.PRODUCT, MediaPurpose.GALLERY);
            responses.add(mediaMapper.toMediaResponse(mediaRepository.save(image)));
            imageProductUrl.add(image.getUrl());
        }

        return ListMediaResponse.builder()
                .mediaResponseList(responses)
                .build();
    }

    @Override
    public MediaResponse imageCategory(MultipartFile thumbNailFile, String categoryId) throws IOException {
        Media media = uploadAndSave(thumbNailFile, categoryId, MediaOwnerType.CATEGORY, MediaPurpose.THUMBNAIL);
        return mediaMapper.toMediaResponse(mediaRepository.save(media));
    }

    @Override
    public MediaResponse changeThumbnail(String oldThumbnailUrl, MultipartFile newThumbnail, String productId)
            throws IOException {
        deleteByUrl(oldThumbnailUrl);
        var exists = mediaRepository.existsByOwnerIdAndMediaPurpose(productId, MediaPurpose.THUMBNAIL);
        if(exists){
            throw new AppException(MediaErrorCode.THUMBNAIL_EXISTS);
        }
        Media thumbnailMedia = uploadAndSave(newThumbnail, productId, MediaOwnerType.PRODUCT, MediaPurpose.THUMBNAIL);
        return MediaResponse.builder()
                .id(thumbnailMedia.getId())
                .ownerId(thumbnailMedia.getOwnerId())
                .ownerType(thumbnailMedia.getOwnerType())
                .url(thumbnailMedia.getUrl())
                .mediaType(thumbnailMedia.getMediaType())
                .mediaPurpose(thumbnailMedia.getMediaPurpose())
                .build();
    }

    @Override
    public void deleteByOwnerId(String ownerId, MediaOwnerType mediaOwnerType) {
        List<Media> medias = mediaRepository.findByOwnerIdAndOwnerType(ownerId, mediaOwnerType);
        if(medias.isEmpty()){
            throw new AppException(MediaErrorCode.CAN_NOT_FIND_MEDIA_BY_PRODUCT);
        }
        for (var media : medias) {
            try {
                String resourceType = media.getMediaType().name().toLowerCase();
                Map result = cloudinary.uploader().destroy(
                        media.getPublicId(),
                        ObjectUtils.asMap("resource_type", resourceType)
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete from Cloudinary: " + e.getMessage(), e);
            }
        }
        mediaRepository.deleteAllInBatch(medias);
    }

    @Override
    public ListMediaResponse getMedia(String ownerId, MediaOwnerType mediaOwnerType) {
        List<Media> medias = mediaRepository.findByOwnerIdAndOwnerType(ownerId, mediaOwnerType);
        List<MediaResponse> mediaResponses = medias.stream()
                .map(media -> MediaResponse.builder()
                        .id(media.getId())
                        .ownerId(media.getOwnerId())
                        .ownerType(media.getOwnerType())
                        .url(media.getUrl())
                        .mediaType(media.getMediaType())
                        .mediaPurpose(media.getMediaPurpose())
                        .build())
                .toList();

        return ListMediaResponse.builder()
                .mediaResponseList(mediaResponses)
                .build();
    }

    @Override
    public void deleteByUrl(String url) {
        Media media = mediaRepository.findByUrl(url);
        if(media == null){
            throw new AppException(MediaErrorCode.CAN_NOT_FIND_MEDIA_BY_URL);
        }
        try {
            cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type",
                    media.getMediaPurpose().name().toLowerCase()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaRepository.deleteById(media.getId());
    }



    private Media uploadAndSave(MultipartFile multipartFile,
                                String ownerId,
                                MediaOwnerType ownerType,
                                MediaPurpose purpose) throws IOException
    {
        Map uploadResult = cloudinary.uploader().upload(
                multipartFile.getBytes(),
                ObjectUtils.asMap("folder", ownerType.name().toLowerCase() + "/" + ownerId));
        String url = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();
        String resourceType = uploadResult.get("resource_type").toString();

        Media media = Media.builder()
                .ownerId(ownerId)
                .ownerType(ownerType)
                .publicId(publicId)
                .url(url)
                .mediaType(MediaType.valueOf(resourceType.toUpperCase()))
                .mediaPurpose(purpose)
                .build();
        return media;
    }
}
