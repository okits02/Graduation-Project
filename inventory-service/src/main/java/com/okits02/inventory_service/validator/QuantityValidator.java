package com.okits02.inventory_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuantityValidator implements ConstraintValidator<QuantityConstraint, Integer> {
    @Override
    public void initialize(QuantityConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if(value == null){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Quantity is not empty!")
                    .addConstraintViolation();
            return false;
        }
        if(value == 0){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Quantity must be greater than 0")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
