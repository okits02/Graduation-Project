package com.example.search_service.mapper;

import com.example.search_service.model.Category;
import com.example.search_service.model.Products;
import com.example.search_service.model.Specification;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
import com.example.search_service.viewmodel.dto.request.CategoryRequest;
import com.example.search_service.viewmodel.dto.request.ProductRequest;
import jdk.jfr.Name;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductsMapper {

    Products toProducts(ProductEventDTO request);
    void updateProduct(@MappingTarget Products products, ProductEventDTO request);

}
