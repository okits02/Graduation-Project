package com.example.product_service.helper;

import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.dto.response.MediaResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.model.Category;
import com.example.product_service.repository.httpsClient.MediaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryMappingHelper {
    private final MediaClient mediaClient;
    public CategoryResponse map(Category category){
        var response = mediaClient.getMedia(category.getId(), MediaOwnerType.CATEGORY).getBody();
        List<MediaResponse> mediaResponse = new ArrayList<>();
        if(response.getResult() != null){
            mediaResponse = response.getResult().getMediaResponseList();
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .special(category.getSpecial())
                .mediaResponse(mediaResponse)
                .childrenId(category.getChildrenId())
                .build();
    }
}
