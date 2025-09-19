package com.example.media_service.service;

import com.example.media_service.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public ProductImageResponse imageProduct(MultipartFile thumbNailFile,
                                             List<MultipartFile> imageProductFile,
                                             String productId) throws IOException;
    public String imageCategory(MultipartFile thumbNailFile, String categoryId) throws IOException;
    public void deleteImageByProductId(String productId);
    public void deleteByUrl(String url);
}
