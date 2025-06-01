package com.example.search_service.viewmodel;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductGetListVM {
    List<ProductGetVM> productGetVMList;
    int currentPages;
    int totalPage;
    int pageSize;
    Long totalElements;
}
