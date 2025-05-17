package com.example.media_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    public String url(MultipartFile multipartFile, String name) throws IOException;
    public void deleteImage(String imgUrl);
    public String getPublicId(String imgUrl);
}
