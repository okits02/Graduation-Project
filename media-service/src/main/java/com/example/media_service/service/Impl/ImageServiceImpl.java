package com.example.media_service.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.media_service.dto.response.ProductImageResponse;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.enums.MediaType;
import com.example.media_service.exception.AppException;
import com.example.media_service.exception.ErrorCode;
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

    @Override
    public ProductImageResponse imageProduct(MultipartFile thumbNailFile,
                                             List<MultipartFile> imageProductFile,
                                             String productId) throws IOException {
        String thumbnailUrl = "";
        List<String> imageProductUrl = new ArrayList<>();
        if(thumbNailFile != null) {
            Media thumbnail = uploadAndSave(thumbNailFile, productId, true);
            thumbnailUrl = thumbnail.getUrl();
            mediaRepository.save(thumbnail);
        }
        for (MultipartFile file : imageProductFile) {
            Media image = uploadAndSave(file, productId, false);
            String url = image.getUrl();
            imageProductUrl.add(url);
            mediaRepository.save(image);
        }
        ProductImageResponse productImageResponse = ProductImageResponse.builder()
                .urlThumbnail(thumbnailUrl)
                .imageUrl(imageProductUrl)
                .build();
        return productImageResponse;
    }

    @Override
    public String imageCategory(MultipartFile thumbNailFile, String categoryId) throws IOException {
        Media media = uploadAndSave(thumbNailFile, categoryId, true);
        mediaRepository.save(media);
        return media.getUrl();
    }

    @Override
    public void deleteImageByProductId(String productId) {
        List<Media> medias = mediaRepository.findByProductId(productId);
        if(medias.isEmpty()){
            throw new AppException(ErrorCode.CAN_NOT_FIND_MEDIA_BY_PRODUCT);
        }
        for (var media : medias){
            try{
                cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        mediaRepository.deleteAll(medias);
    }

    @Override
    public void deleteByUrl(String url) {
        Media media = mediaRepository.findByUrl(url);
        if(media == null){
            throw new AppException(ErrorCode.CAN_NOT_FIND_MEDIA_BY_URL);
        }
        try {
            cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type",
                    media.getMediaPurpose().name().toLowerCase()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaRepository.deleteById(media.getId());
    }


    private Media uploadAndSave(MultipartFile multipartFile, String productId, boolean isThumbnail) throws IOException
    {
        var uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                ObjectUtils.asMap("folder", "product/" + productId));
        String url = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();
        String resourceType = uploadResult.get("resource_type").toString();

        Media media = Media.builder()
                .productId(productId)
                .publicId(publicId)
                .url(url)
                .mediaType(MediaType.valueOf(resourceType.toUpperCase()))
                .mediaPurpose(MediaPurpose.valueOf(isThumbnail ? "THUMBNAIL" : "GALLERY"))
                .build();
        return media;
    }
}
