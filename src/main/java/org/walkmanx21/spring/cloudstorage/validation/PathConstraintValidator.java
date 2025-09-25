package org.walkmanx21.spring.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PathConstraintValidator implements ConstraintValidator<ValidPath, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        return !path.contains("//");
    }
}
