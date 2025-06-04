package com.example.media_service.service;

import com.example.media_service.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public String url(MultipartFile multipartFile, String name) throws IOException;
    public ProductImageResponse imageProduct(MultipartFile thumbNailFile,
                                             List<MultipartFile> imageProductFile,
                                             String name) throws IOException;
    public void deleteImage(String imgUrl);
    public String getPublicId(String imgUrl);
}
