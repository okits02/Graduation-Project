package com.example.product_service.helper;


import com.example.product_service.dto.request.GetMediaRequest;
import com.example.product_service.dto.response.MediaResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.exceptions.ProductErrorCode;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.mapper.ProductVariantsMapper;
import com.example.product_service.model.ProductVariants;
import com.example.product_service.model.Products;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.repository.httpsClient.MediaClient;
import com.example.product_service.service.ProductVariantsService;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMappingHelper {
    private final CategoryRepository categoryRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final ProductVariantsMapper productVariantsMapper;
    private final CategoryMapper categoryMapper;
    private final MediaClient mediaClient;
    public ProductResponse map(final Products products) {
        var listCategory = products.getCategoryId().stream()
                .map(categoryRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(categoryMapper::toCategoryResponse).toList();
        var responses = mediaClient.getMedia(products.getId(), MediaOwnerType.PRODUCT).getBody();
        List<MediaResponse> listMedia = new ArrayList<>();
        if(responses.getResult() != null){
            listMedia = responses.getResult().getMediaResponseList();
        }
        List<ProductVariants> productVariants = productVariantsRepository.findByProductId(products.getId());
        if(productVariants == null || productVariants.isEmpty()){
            throw new AppException(ProductErrorCode.PRODUCT_VARIANTS_NOT_FOUND);
        }
        List<ProductVariantsResponse> variantsResponses = productVariants.stream()
                .map(productVariantsMapper::toProductVariantsResponse).toList();
        variantsResponses.forEach(variant -> {
            var variantMediaResponse =
                    mediaClient.getMedia(variant.getSku(), MediaOwnerType.PRODUCT_VARIANT).getBody();

            if (variantMediaResponse != null
                    && variantMediaResponse.getResult() != null
                    && !variantMediaResponse.getResult().getMediaResponseList().isEmpty()) {

                String thumbnailUrl = variantMediaResponse
                        .getResult()
                        .getMediaResponseList()
                        .get(0)
                        .getUrl();

                variant.setThumbnail(thumbnailUrl);
            }
        });

        return ProductResponse.builder()
                .id(products.getId())
                .name(products.getName())
                .brandName(products.getBrandName())
                .description(products.getDescription())
                .listCategory(listCategory)
                .videoUrl(products.getVideoUrl())
                .mediaList(listMedia)
                .warranty(products.getWarranty())
                .specifications(products.getSpecifications())
                .variantsResponses(variantsResponses)
                .createAt(products.getCreateAt())
                .updateAt(products.getUpdateAt())
                .build();
    }
}
