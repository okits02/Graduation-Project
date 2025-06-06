package com.example.search_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specification {
    @Field(type = FieldType.Keyword)
    String key;
    @Field(type = FieldType.Keyword)
    String value;
}
