package com.example.media_service.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.media_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;
    @Override
    public String url(MultipartFile multipartFile, String name) throws IOException {
        String url = "";
        url = cloudinary.uploader()
                .upload(multipartFile.getBytes(), Map.of("public_id", name))
                .get("url")
                .toString();
        return url;
    }

    @Override
    public void deleteImage(String imgUrl) {
        String publicId = getPublicId(imgUrl);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPublicId(String imgUrl) {
        String[] part = imgUrl.split("/");
        String publicIdWithFormat = part[part.length - 1];
        String[] publicIdAndFormat = publicIdWithFormat.split("\\.");
        return publicIdAndFormat[0];
    }
}
