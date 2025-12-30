package com.example.product_service.validator;

import com.example.product_service.dto.request.ProductVariantsRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductVariantValidator implements ConstraintValidator<ValidProductVariant, ProductVariantsRequest> {
    @Override
    public void initialize(ValidProductVariant constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(ProductVariantsRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if (request == null) return true;

        constraintValidatorContext.disableDefaultConstraintViolation();

        if (request.getAction() == null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "action is required"
            ).addPropertyNode("action").addConstraintViolation();
            return false;
        }

        constraintValidatorContext.disableDefaultConstraintViolation();
        switch (request.getAction()){
            case CREATE ->  {
                if(request.getSku() != null && !request.getSku().isBlank()){
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "sku must be null when action CREATE"
                    ).addPropertyNode("sku").addConstraintViolation();
                    return false;
                }

                if(request.getVariantName() == null || request.getVariantName().isBlank()
                        || request.getColor() == null || request.getColor().isBlank() || request.getPrice() == null){
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "variantName, color and price are required when action is Create"
                    ).addConstraintViolation();
                    return false;
                }
            }
            case UPDATE, DELETE -> {
                if (request.getSku() == null || request.getSku().isBlank()) {
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "sku is required when action is " + request.getAction()
                    ).addPropertyNode("sku").addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }
}
