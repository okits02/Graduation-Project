package com.example.media_service.mapper;

import com.example.media_service.dto.response.MediaResponse;
import com.example.media_service.model.Media;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaResponse toMediaResponse(Media media);
}
