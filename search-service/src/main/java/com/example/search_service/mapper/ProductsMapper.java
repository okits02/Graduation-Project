package com.example.search_service.mapper;

import com.example.search_service.model.Category;
import com.example.search_service.model.Products;
import com.example.search_service.model.Specification;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
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
    @Mapping(target = "categories", source = "categories", qualifiedByName = "mapCategories")
    @Mapping(target = "specifications", source = "specifications", qualifiedByName = "mapSpecifications")
    Products toProducts(ProductRequest productRequest);

    @Mapping(target = "categories", source = "categories", qualifiedByName = "mapCategories")
    @Mapping(target = "specifications", source = "specifications", qualifiedByName = "mapSpecifications")
    void updateProduct(@MappingTarget Products products, ProductRequest request);

    @Named("mapCategories")
    default List<Category> mapCategories(List<String> category) {
        if (category == null) {
            return null;
        }
        return category.stream().map(Category::new)
                .collect(Collectors.toList());
    }


    @Named("mapSpecifications")
    default List<Specification> mapSpecifications(Map<String, String> specificatons)
    {
        if(specificatons == null)
        {
            return null;
        }
        return specificatons.entrySet().stream()
                .map(stringStringEntry ->
                        new Specification(stringStringEntry.getKey(), stringStringEntry.getValue()))
                .collect(Collectors.toList());
    }
}
