package com.example.product_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductVariantValidator.class)
@Documented
public @interface ValidProductVariant {
    String message() default "Invalid product variant request";
    Class<?>[] group() default {};
    Class<? extends Payload>[] payload() default {};
}
