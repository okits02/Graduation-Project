package com.example.product_service.service.Impl;

import com.example.product_service.dto.request.BrandCreationRequest;
import com.example.product_service.dto.response.BrandResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.exceptions.ProductErrorCode;
import com.example.product_service.mapper.BrandMapper;
import com.example.product_service.model.Brand;
import com.example.product_service.model.Category;
import com.example.product_service.repository.BrandRepository;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.httpsClient.MediaClient;
import com.example.product_service.service.BrandService;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final BrandMapper brandMapper;
    private final MediaClient mediaClient;

    @Override
    public BrandResponse save(BrandCreationRequest request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new AppException(ProductErrorCode.BRAND_EXISTS);
        }
        Brand brand = Brand.builder()
                .id(new ObjectId().toHexString())
                .name(request.getName())
                .build();
        Category root = categoryRepository.findByName("root");
        List<Category> categories = categoryRepository.findAllById(request.getCategoryId());
        boolean isValid = categories.size() == request.getCategoryId().size()
                && categories.stream().allMatch(c -> root.getId().equals(c.getParentId()));

        if (!isValid) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_UNDER_ROOT);
        }
        brand.setCategoryId(request.getCategoryId());
        brandRepository.save(brand);
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .categoryId(brand.getCategoryId())
                .build();
    }

    @Override
    public BrandResponse update(BrandCreationRequest request) {
        if (!brandRepository.existsByName(request.getName())) {
            throw new AppException(ProductErrorCode.BRAND_NOT_EXISTS);
        }
        Brand brand = brandRepository.findByName(request.getName());
        brandMapper.updateBrand(brand, request);
        Category root = categoryRepository.findByName("root");
        List<Category> categories = categoryRepository.findAllById(request.getCategoryId());
        boolean isValid = categories.size() == request.getCategoryId().size()
                && categories.stream().allMatch(c -> root.getId().equals(c.getParentId()));

        if (!isValid) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_UNDER_ROOT);
        }
        brand.setCategoryId(request.getCategoryId());
        brandRepository.save(brand);
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .thumbnailUrl(getThumbnail(brand))
                .categoryId(brand.getCategoryId())
                .build();
    }

    @Override
    public List<BrandResponse> getList() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream().map(m -> BrandResponse
                .builder()
                .id(m.getId())
                .name(m.getName())
                .thumbnailUrl(getThumbnail(m))
                .categoryId(m.getCategoryId())
                .build()).toList();
    }

    @Override
    public void delete(String name) {
        if (!brandRepository.existsByName(name)) {
            throw new AppException(ProductErrorCode.BRAND_NOT_EXISTS);
        }
        Brand brand = brandRepository.findByName(name);
        brandRepository.delete(brand);
    }

    @Override
    public void removeCategoryFromBrand(String categoryId) {
        List<Brand> brands = brandRepository.findByCategoryId(categoryId);

        for (Brand brand : brands) {
            if (brand.getCategoryId() != null && brand.getCategoryId().remove(categoryId)) {
                brandRepository.save(brand);
            }
        }
    }

    private String getThumbnail(Brand brand) {
        var variantMediaResponse = mediaClient.getMedia(brand.getId(), MediaOwnerType.PRODUCT_VARIANT).getBody();
        String thumbnailUrl = null;
        if (variantMediaResponse != null
                && variantMediaResponse.getResult() != null
                && !variantMediaResponse.getResult().getMediaResponseList().isEmpty()) {

            thumbnailUrl = variantMediaResponse
                    .getResult()
                    .getMediaResponseList()
                    .get(0)
                    .getUrl();
        }
        return thumbnailUrl;
    }
}
