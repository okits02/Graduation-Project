package com.example.promotion_service.validator;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.enums.DiscountType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PromotionValidator implements ConstraintValidator<ValidPromotion, PromotionCreationRequest> {
    @Override
    public void initialize(ValidPromotion constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PromotionCreationRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if(request == null) return  true;
        constraintValidatorContext.disableDefaultConstraintViolation();
        if(request.getPromotionKind() == null){
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Kind is required"
            ).addPropertyNode("action").addConstraintViolation();
            return false;
        }
        switch (request.getPromotionKind()){
            case VOUCHER -> {
                return validateVoucher(request, constraintValidatorContext);
            }

            case AUTO -> {
                return validateAuto(request, constraintValidatorContext);
            }
        }
        return false;
    }

    private boolean validateVoucher(PromotionCreationRequest request,
                                    ConstraintValidatorContext constraintValidatorContext){
        if (request.getDiscountPercent() > 0 && request.getFixedAmount() > 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Only one of discountPercent or fixedAmount is allowed"
            ).addConstraintViolation();
            return false;
        }

        if (request.getDiscountPercent() <= 0 && request.getFixedAmount() <= 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "discountPercent or fixedAmount is required"
            ).addConstraintViolation();
            return false;
        }
        switch (request.getDiscountType()){
            case DISCOUNT_PERCENT -> {
                if(request.getDiscountPercent() <= 0 && request.getFixedAmount() >=0){
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "Discount percent need great than zero and fixedAmount must equals zero"
                    ).addConstraintViolation();
                    return false;
                }
            }
            case FIXED_AMOUNT -> {
                if(request.getDiscountPercent() >= 0 && request.getFixedAmount() <=0){
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "fixedAmount need great than zero and discountPercent must equals zero"
                    ).addConstraintViolation();
                    return false;
                }
            }
        }

        if (request.getUsageLimited() <= 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "usageLimited must be greater than 0 for VOUCHER"
            ).addPropertyNode("usageLimited").addConstraintViolation();
            return false;
        }

        if (request.getUsageLimitPerUser() <= 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "usageLimitPerUser must be greater than 0 for VOUCHER"
            ).addPropertyNode("usageLimitPerUser").addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean validateAuto(PromotionCreationRequest request,
                                 ConstraintValidatorContext constraintValidatorContext){
        if (request.getDiscountPercent() > 0 && request.getFixedAmount() > 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Only one of discountPercent or fixedAmount is allowed"
            ).addConstraintViolation();
            return false;
        }

        if (request.getDiscountPercent() <= 0 && request.getFixedAmount() <= 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "discountPercent or fixedAmount is required"
            ).addConstraintViolation();
            return false;
        }
        switch (request.getDiscountType()){
            case DISCOUNT_PERCENT -> {
                if(request.getDiscountPercent() <= 0 && request.getFixedAmount() >=0){
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "Discount percent need great than zero and fixedAmount must equals zero"
                    ).addConstraintViolation();
                    return false;
                }
            }
            case FIXED_AMOUNT -> {
                if(request.getDiscountPercent() >= 0 && request.getFixedAmount() <=0){
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "fixedAmount need great than zero and discountPercent must equals zero"
                    ).addConstraintViolation();
                    return false;
                }
            }
        }
        switch (request.getApplyTo()) {
            case Product -> {
                if (request.getProductId() == null || request.getProductId().isEmpty()) {
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "productId is required when applyTo is PRODUCT"
                    ).addPropertyNode("productId").addConstraintViolation();
                    return false;
                }
            }
            case Category -> {
                if (request.getCategoryId() == null || request.getCategoryId().isEmpty()) {
                    constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "categoryId is required when applyTo is CATEGORY"
                    ).addPropertyNode("categoryId").addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
