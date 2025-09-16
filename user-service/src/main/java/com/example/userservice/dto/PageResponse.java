package com.example.userservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse <T>{
    int currentPage;
    int totalPage;
    int pageSize;
    Long totalElements;

    @Builder.Default
    private List<T> Data = new ArrayList<>();
}
