package com.example.search_service.Repository;

import com.example.search_service.model.Products;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepository extends ElasticsearchRepository<Products, String> {

}
