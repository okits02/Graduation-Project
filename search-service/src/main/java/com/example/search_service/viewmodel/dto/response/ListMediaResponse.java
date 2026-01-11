package com.example.search_service.viewmodel.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListMediaResponse {
    List<MediaResponse> mediaResponseList;
}
