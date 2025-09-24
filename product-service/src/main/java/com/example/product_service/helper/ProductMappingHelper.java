package com.example.product_service.helper;


import com.example.product_service.dto.request.GetMediaRequest;
import com.example.product_service.dto.response.MediaResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.model.Products;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.httpsClient.MediaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMappingHelper {
    private final CategoryRepository categoryRepository;
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
        return ProductResponse.builder()
                .id(products.getId())
                .name(products.getName())
                .listCategory(listCategory)
                .avgRating(products.getAvgRating())
                .color(products.getColor())
                .description(products.getDescription())
                .sold(products.getSold())
                .listPrice(products.getListPrice())
                .avgRating(products.getAvgRating())
                .mediaList(listMedia)
                .quantity(products.getQuantity())
                .specifications(products.getSpecifications())
                .build();
    }
}
