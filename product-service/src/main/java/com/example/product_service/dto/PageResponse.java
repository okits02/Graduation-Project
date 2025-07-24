package com.example.product_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    int currentPage;
    int totalPage;
    int pageSize;
    Long totalElements;

    @Builder.Default
    private List<T> data = Collections.emptyList();
}
