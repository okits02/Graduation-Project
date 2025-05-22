package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.model.Category;
import com.example.product_service.model.Image;
import com.example.product_service.model.Products;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    public Image createProductImage(Products products, MultipartFile multipartFile, int n);
    public String createCategoryImage(CategoryRequest categoryRequest, MultipartFile multipartFile, int n);
    public void deleteImage(String id);
}
