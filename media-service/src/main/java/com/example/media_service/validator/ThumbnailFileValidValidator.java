package com.example.media_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class ThumbnailFileValidValidator implements ConstraintValidator<ThumbnailFileValidConstraint, MultipartFile> {
    String[] allowedTypes;
    String message;

    @Override
    public void initialize(ThumbnailFileValidConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    @SneakyThrows
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (value == null || value.getContentType() == null) {
            context.buildConstraintViolationWithTemplate("File không được để trống hoặc không xác định loại")
                    .addConstraintViolation();
            return false;
        }
        boolean matchedType = Arrays.stream(allowedTypes)
                .anyMatch(type -> type.equalsIgnoreCase(value.getContentType()));

        if (!matchedType) {
            context.buildConstraintViolationWithTemplate("Định dạng file không được hỗ trợ: "
                            + value.getContentType())
                    .addConstraintViolation();
            return false;
        }
        try {
            BufferedImage image = ImageIO.read(value.getInputStream());
            if (image == null) {
                context.buildConstraintViolationWithTemplate("File không phải là ảnh hợp lệ")
                        .addConstraintViolation();
                return false;
            }
        } catch (IOException e) {
            context.buildConstraintViolationWithTemplate("Không thể đọc file ảnh: " + e.getMessage())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
