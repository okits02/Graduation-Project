package com.example.product_service.service.Impl;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.exceptions.ErrorCode;
import com.example.product_service.model.Image;
import com.example.product_service.model.Products;
import com.example.product_service.repository.ImageRepository;
import com.example.product_service.repository.httpClient.MediaClient;
import com.example.product_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final MediaClient mediaClient;

    @Override
    public Image createProductImage(Products products, MultipartFile multipartFile, int n) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        ResponseEntity<String> response =
                mediaClient.url(authHeader, multipartFile, "product_" + products.getId() + "." + n);
        Image image = new Image();
        image.setUrlImg(response.getBody());
        image.setProduct(products);
        image.setNameImg(products.getName() + "_image" + "." + n);
        image.setIcon(false);
        return imageRepository.save(image);
    }

    @Override
    public String createCategoryImage(CategoryRequest categoryRequest, MultipartFile multipartFile, int n) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        ResponseEntity<String> response =
                mediaClient.url(authHeader, multipartFile, "product_" + categoryRequest.getId() + "." + n);
        return  response.getBody();
    }

    @Override
    public void deleteImage(String id) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        Optional<Image> image = imageRepository.findById(id);
        if(image.isEmpty())
        {
            throw new AppException(ErrorCode.IMAGE_NOT_EXISTS);
        }
        ResponseEntity<?> response = mediaClient.deleteImage(authHeader, image.get().getUrlImg());
        imageRepository.deleteById(id);
    }
}
