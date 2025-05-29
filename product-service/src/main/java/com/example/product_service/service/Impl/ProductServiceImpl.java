package com.example.product_service.service.Impl;

import com.example.product_service.dto.PageResponse;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.exceptions.ErrorCode;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.model.Image;
import com.example.product_service.model.Products;
import com.example.product_service.repository.ImageRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.CategoryService;
import com.example.product_service.service.ImageService;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final CategoryService categoryService;

    @Override
    public PageResponse<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = productRepository.findAll(pageable);
        return PageResponse.<ProductResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(productMapper::toProductResponse).toList())
                .build();
    }

    @Override
    public ProductResponse getById(String productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));
        ProductResponse productResponse = productMapper.toProductResponse(product);
        productResponse.setListCategory(categoryService.getCategoryHierarchy(product.getCategoryId()));
        return productResponse;
    }

    @Override
    public Products createProduct(MultipartFile thumbNail, List<MultipartFile> multipartFile, ProductRequest request) {
        if (productRepository.existsById(request.getId())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTS);
        }
        Products products = productMapper.toProduct(request);
        Products newProducts = productRepository.save(products);
        List<String> imageList = new ArrayList<>();
        if (thumbNail != null && !thumbNail.isEmpty()) {
            Image thumbImage = imageService.createProductImage(newProducts, thumbNail, 0);
            thumbImage.setIcon(true);
            imageRepository.save(thumbImage);
            imageList.add(thumbImage.getId());
        }
        int index = 1;
        for (MultipartFile file : multipartFile) {
            if (file != null && !file.isEmpty()) {
                Image image = imageService.createProductImage(newProducts, file, index++);
                image.setIcon(false);
                imageRepository.save(image);
                imageList.add(image.getId());
            }
        }
        newProducts.setCategoryId(request.getCategory().getId());
        newProducts.setImageList(imageList);
        productRepository.save(newProducts);
        return newProducts;
    }

    @Override
    public ProductResponse updateProduct(MultipartFile thumbNails, List<MultipartFile> multipartFiles,
                                         ProductUpdateRequest request) {
        Products products = productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ErrorCode.PRODUCT_NOT_EXISTS));
        productMapper.updateProduct(products, request);
        if(!request.getImagesToDelete().isEmpty())
        {
            for(String url : request.getImagesToDelete())
            {
                Image image = imageRepository.findByUrlImg(url);
                if(image == null)
                {
                    throw new AppException(ErrorCode.IMAGE_NOT_EXISTS);
                }
                imageRepository.deleteById(image.getId());
            }
        }
        List<String> newImages = new ArrayList<>();

        if (thumbNails != null && !thumbNails.isEmpty()) {
            Image thumbImg = imageService.createProductImage(products, thumbNails, 0);
            thumbImg.setIcon(true);
            imageRepository.save(thumbImg);
            newImages.add(thumbImg.getId());
        }
        int index = 1;
        for (MultipartFile file : multipartFiles) {
            Image img = imageService.createProductImage(products, file, index++);
            img.setIcon(false);
            imageRepository.save(img);
            newImages.add(img.getId());
        }
        products.setCategoryId(request.getCategoryId());
        products.setImageList(newImages);
        productRepository.save(products);
        return productMapper.toProductResponse(products);
    }

    @Override
    public void DeleteProduct(String productId) {
        List<Image> imageList = imageRepository.findByProductId(productId);
        for(Image image : imageList)
        {
            imageService.deleteImage(image.getId());
        }
        productRepository.deleteById(productId);
    }
}
