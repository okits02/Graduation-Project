package com.example.media_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ListFileImageValidator implements ConstraintValidator<ListFileImageConstraint, List<MultipartFile>> {
    private String[] allowedTypes;
    private String message;
    private int index = 0;
    @Override
    public void initialize(ListFileImageConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    @SneakyThrows
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if(files == null){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The list of photos is not empty!")
                    .addConstraintViolation();
            return false;
        }
        for (MultipartFile multipartFile : files){
            if(multipartFile == null || multipartFile.getContentType() == null){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                return false;
            }
            boolean match = Arrays.stream(allowedTypes).anyMatch(type -> type
                    .equalsIgnoreCase(multipartFile.getContentType()));
            if(!match){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("can not read image's" + index)
                        .addConstraintViolation();
                return false;
            }

            try {
                BufferedImage image = ImageIO.read(multipartFile.getInputStream());
                if(image == null) return false;
            }catch (IOException e){
                return false;
            }
            index++;
        }
        return true;
    }
}
