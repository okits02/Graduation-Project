package com.example.promotion_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PromotionValidator.class)
@Documented
public @interface ValidPromotion {
    String message() default "Invalid promotion request";
    Class<?>[] group() default {};
    Class<? extends Payload>[] payload() default {};
}
