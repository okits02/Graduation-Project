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
import com.example.product_service.repository.httpClient.MediaClient;
import com.example.product_service.service.ImageService;
import com.example.product_service.service.ProductService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ImageService imageService;
    private final ImageRepository imageRepository;


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
        Optional<Products> products = productRepository.findById(productId);
        if(products.isEmpty())
        {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        return productMapper.toProductResponse(products);
    }

    @Override
    public Products createProduct(MultipartFile thumbNail, List<MultipartFile> multipartFile, ProductRequest request) {
        productRepository.findById(request.getId()).orElseThrow(()->
                new AppException(ErrorCode.PRODUCT_EXISTS));
        Products products = productMapper.toProduct(request);
        Products newProducts = productRepository.save(products);
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        List<Image> imageList = new ArrayList<>();

        if (thumbNail != null && !thumbNail.isEmpty()) {
            Image thumbImage = imageService.createImage(newProducts, thumbNail, 0);
            thumbImage.setIcon(true);
            imageList.add(thumbImage);
        }

        int index = 1;
        for (MultipartFile file : multipartFile) {
            if (file != null && !file.isEmpty()) {
                Image image = imageService.createImage(newProducts, file, index++);
                image.setIcon(false);
                imageList.add(image);
            }
        }

        newProducts.setImageList(imageList);
        productRepository.save(products);
        return products;
    }

    @Override
    public ProductResponse updateProduct(MultipartFile thumbNails, List<MultipartFile> multipartFiles, ProductUpdateRequest request) {
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

        List<Image> newImages = new ArrayList<>();

        if (thumbNails != null && !thumbNails.isEmpty()) {
            Image thumbImg = imageService.createImage(products, thumbNails, 0);
            thumbImg.setIcon(true);
            newImages.add(thumbImg);
        }

        int index = 1;
        for (MultipartFile file : multipartFiles) {
            Image img = imageService.createImage(products, file, index++);
            img.setIcon(false);
            newImages.add(img);
        }

        products.setImageList(newImages);
        productRepository.save(products);
        return productMapper.toProductResponse(productRepository.save(products));
    }

    @Override
    public void DeleteProduct(String productId) {
        List<Image> imageList = imageRepository.findByProductsId(productId);
        for(Image image : imageList)
        {
            imageService.deleteImage(image.getId());
        }
        productRepository.deleteById(productId);
    }
}
