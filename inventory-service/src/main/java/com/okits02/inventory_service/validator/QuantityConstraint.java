package com.okits02.inventory_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {QuantityValidator.class}
)
public @interface QuantityConstraint {
    String message() default "Invalid file!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
