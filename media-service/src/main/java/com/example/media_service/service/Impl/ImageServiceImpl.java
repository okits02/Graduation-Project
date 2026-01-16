package com.example.media_service.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.media_service.dto.request.AvatarUserCreationRequest;
import com.example.media_service.dto.request.BannerCreationRequest;
import com.example.media_service.dto.response.BannerResponse;
import com.example.media_service.dto.response.ListMediaResponse;
import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import com.example.media_service.repository.httpClient.ProfileClient;
import com.example.media_service.repository.httpClient.RatingClient;
import com.example.media_service.repository.httpClient.UserClient;
import com.okits02.common_lib.exception.AppException;
import com.example.media_service.exception.MediaErrorCode;
import com.example.media_service.mapper.MediaMapper;
import com.example.media_service.model.Media;
import com.example.media_service.repository.MediaRepository;
import com.example.media_service.service.ImageService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    private final UserClient userClient;
    private final RatingClient ratingClient;

    @Override
    public ListMediaResponse imageProduct(List<MultipartFile> imageProductFile,
                                          String productId) throws IOException {
        List<String> imageProductUrl = new ArrayList<>();
        List<MediaResponse> responses = new ArrayList<>();
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
    public MediaResponse uploadImage(MultipartFile thumbNailFile,
                                     String ownerId, MediaOwnerType mediaOwnerType) throws IOException {
        Media media = uploadAndSave(thumbNailFile,ownerId, mediaOwnerType, MediaPurpose.THUMBNAIL);
        return mediaMapper.toMediaResponse(mediaRepository.save(media));
    }

    @Override
    public MediaResponse changeThumbnail(String oldThumbnailUrl, MultipartFile newThumbnail, String sku)
            throws IOException {
        Media oldThumbnail = mediaRepository.findByUrl(oldThumbnailUrl);
        if(oldThumbnail == null){
            throw new AppException(MediaErrorCode.CAN_NOT_FIND_MEDIA_BY_URL);
        }
        mediaRepository.deleteById(oldThumbnail.getId());
        var exists = mediaRepository.existsByOwnerIdAndMediaPurpose(sku, MediaPurpose.THUMBNAIL);
        if(exists){
            throw new AppException(MediaErrorCode.THUMBNAIL_EXISTS);
        }
        Media thumbnailMedia = uploadAndSave(newThumbnail, sku, MediaOwnerType.PRODUCT_VARIANT, MediaPurpose.THUMBNAIL);
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
        List<Media> medias = mediaRepository.findByOwnerIdAndOwnerType(ownerId, mediaOwnerType.getCode());
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
        List<Media> medias = mediaRepository.findByOwnerIdAndOwnerType(ownerId, mediaOwnerType.getCode());
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
    public BannerResponse createBanner(BannerCreationRequest request) throws IOException {
        Media image = null;
        image = uploadAndSave(request.getImageBanner(), request.getOwnerId(),
                request.getOwnerType(), MediaPurpose.BANNER);
        image.setMediaType(MediaType.IMAGE);
        Media saveMedia = mediaRepository.save(image);
        return BannerResponse.builder()
                .id(saveMedia.getId())
                .ownerId(saveMedia.getOwnerId())
                .mediaOwnerType(image.getOwnerType())
                .bannerUrl(saveMedia.getUrl())
                .build();
    }

    @Override
    public MediaResponse createAvatarUser(AvatarUserCreationRequest request) throws IOException {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var userResponse = userClient.getUserId(authHeader);
        if(userResponse.getCode() != 200)
        {
            throw new RuntimeException("call to user-client failed");
        }
        List<Media> media = mediaRepository.findByOwnerIdAndOwnerType(userResponse.getResult().getUserId(),
                MediaOwnerType.USER.toString());
        if(!media.isEmpty()){
            deleteByUrl(media.get(0).getUrl());
        }
        Media image = uploadAndSave(request.getAvatarFile(), userResponse.getResult().getUserId(),
                MediaOwnerType.USER, MediaPurpose.GALLERY);
        return mediaMapper.toMediaResponse(image);
    }

    @Override
    public List<BannerResponse> getAllBanner() {
        List<Media> medias = mediaRepository.findAllByMediaPurpose(MediaPurpose.BANNER.name());
        if(medias == null || medias.isEmpty()){
            throw new AppException(MediaErrorCode.BANNER_IS_NOT_EXISTS);
        }

        return medias.stream()
                .map(media -> BannerResponse.builder()
                        .id(media.getId())
                        .ownerId(media.getOwnerId())
                        .mediaOwnerType(media.getOwnerType())
                        .bannerUrl(media.getUrl())
                        .mediaPurpose(media.getMediaPurpose())
                        .build())
                .toList();
    }

    @Override
    public void updateImageForRating(List<MultipartFile> multipartFiles, String ratingId) throws IOException {
        if(multipartFiles == null || ratingId == null) return;
        List<String> ratingImageUrl = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            Media image = uploadAndSave(file, ratingId, MediaOwnerType.RATING, MediaPurpose.GALLERY);
            ratingImageUrl.add(image.getUrl());
        }
        ratingClient.updateImageForRating(ratingImageUrl, ratingId);
    }

    @Override
    public void updateImageForComment(List<MultipartFile> multipartFiles, String commentId) throws IOException {
        if(multipartFiles == null || commentId == null) return;
        List<String> commentImageUrl = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            Media image = uploadAndSave(file, commentId, MediaOwnerType.RATING, MediaPurpose.GALLERY);
            commentImageUrl.add(image.getUrl());
        }
        ratingClient.updateImageForComment(commentImageUrl, commentId);
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
