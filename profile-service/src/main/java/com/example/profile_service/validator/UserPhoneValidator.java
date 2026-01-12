package com.example.profile_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserPhoneValidator implements ConstraintValidator<UserPhoneConstraint, String> {

    private int min;

    @Override
    public void initialize(UserPhoneConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(s == null || s.length() < min ) {
            return false;
        }
        return s.matches("^0\\d{9}$");
    }
}
