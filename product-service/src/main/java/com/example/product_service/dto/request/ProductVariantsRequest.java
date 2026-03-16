package com.example.product_service.dto.request;

import com.example.product_service.enums.VariantAction;
import com.example.product_service.validator.ValidProductVariant;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@ValidProductVariant
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantsRequest {
    VariantAction action;
    String sku;
    String variantName;
    BigDecimal price;
    List<SpecificationRequest> bestSpecifications;
}
