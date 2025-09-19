package com.example.media_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class VideoValidValidator implements ConstraintValidator<VideoValidConstraint, MultipartFile> {
    String[] allowedTypes;
    String message;
    @Override
    public void initialize(VideoValidConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if(value == null || value.getContentType()  == null){
            context.buildConstraintViolationWithTemplate("File không được để trống hoặc không xác định loại")
                    .addConstraintViolation();
            return false;
        }
        boolean matchedType = Arrays.stream(allowedTypes)
                .anyMatch(type -> type.equalsIgnoreCase(value.getContentType()));
        if(!matchedType){
            context.buildConstraintViolationWithTemplate("Định dạng file không được hỗ trợ: "
                    + value.getContentType()).addConstraintViolation();
            return false;
        }
        if (value.getSize() > 100 * 1024 * 1024) {
            context.buildConstraintViolationWithTemplate("Kích thước video vượt quá giới hạn (100MB)")
                    .addConstraintViolation();
            return false;
        }
         return true;
    }
}
