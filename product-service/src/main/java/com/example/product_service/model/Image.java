package com.example.product_service.model;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {
    @Id
    String id;
    @Field("name_image")
    String nameImg;
    @Field("is_thumbnail")
    boolean icon;
    @Field("url_image")
    String urlImg;
    @Field("id_product")
    String productId;
}
