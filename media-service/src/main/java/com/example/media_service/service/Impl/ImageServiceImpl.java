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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            thumbnail.setPosition(0);
            responses.add(mediaMapper.toMediaResponse(mediaRepository.save(thumbnail)));
            thumbnailUrl = thumbnail.getUrl();
        }
        Integer currentPosition = mediaRepository.findMaxPositionByOwnerIdAndPurpose(productId, MediaPurpose.GALLERY.name())
                .orElse(0);
        for (MultipartFile file : imageProductFile) {
            currentPosition++;
            Media image = uploadAndSave(file, productId, MediaOwnerType.PRODUCT, MediaPurpose.GALLERY);
            image.setPosition(currentPosition);
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
        Media oldThumbnail = mediaRepository.findByUrl(oldThumbnailUrl);
        if(oldThumbnail == null){
            throw new AppException(MediaErrorCode.CAN_NOT_FIND_MEDIA_BY_URL);
        }
        mediaRepository.deleteById(oldThumbnail.getId());
        var exists = mediaRepository.existsByOwnerIdAndMediaPurpose(productId, MediaPurpose.THUMBNAIL);
        if(exists){
            throw new AppException(MediaErrorCode.THUMBNAIL_EXISTS);
        }
        Media thumbnailMedia = uploadAndSave(newThumbnail, productId, MediaOwnerType.PRODUCT, MediaPurpose.THUMBNAIL);
        thumbnailMedia.setPosition(0);
        return mediaMapper.toMediaResponse(mediaRepository.save(thumbnailMedia));
    }

    @Override
    public ListMediaResponse changeImageProduct(List<MultipartFile> listFileImage, String productId) throws IOException {
        Optional<Integer> currentPosition = mediaRepository
                .findMaxPositionByOwnerIdAndPurpose(productId, MediaPurpose.GALLERY.name());
        List<MediaResponse> responses = new ArrayList<>();
        Integer i = currentPosition.get();
        for(MultipartFile file : listFileImage) {
            Media media = uploadAndSave(file, productId, MediaOwnerType.PRODUCT, MediaPurpose.GALLERY);
            media.setPosition(i + 1);
            responses.add(mediaMapper.toMediaResponse(mediaRepository.save(media)));
            i++;
        }
        return ListMediaResponse.builder()
                .mediaResponseList(responses)
                .build();
    }

    @Override
    public void deleteByOwnerId(String ownerId, MediaOwnerType mediaOwnerType) {
        List<Media> medias = mediaRepository.findByOwnerIdAndOwnerType(ownerId, mediaOwnerType.name());
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
        List<Media> medias = mediaRepository.findByOwnerIdAndOwnerType(ownerId, mediaOwnerType.name());
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
    @Transactional
    public void deleteByUrl(String url) {
        Media media = mediaRepository.findByUrl(url);
        if(media == null){
            throw new AppException(MediaErrorCode.CAN_NOT_FIND_MEDIA_BY_URL);
        }
        mediaRepository.reindexAfterDelete(media.getOwnerId(), url);
        try {
            cloudinary.uploader().destroy(
                    media.getPublicId(),
                    ObjectUtils.asMap("resource_type", media.getMediaType().name().toLowerCase())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaRepository.deleteById(media.getId());
    }

    @Transactional
    public void changePosition(String mediaId, Integer newPosition) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException(MediaErrorCode.CAN_NOT_FIND_MEDIA_BY_ID));

        int oldPos = media.getPosition();
        String ownerId = media.getOwnerId();

        if (oldPos == newPosition) return;

        if (oldPos < newPosition) {
            mediaRepository.shiftDownPositions(ownerId, oldPos, newPosition);
        } else {
            mediaRepository.shiftUpPositions(ownerId, oldPos, newPosition);
        }
        media.setPosition(newPosition);
        mediaRepository.save(media);
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
