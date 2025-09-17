package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;

@Component
public class PathValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PathRequestDto pathRequestDto = (PathRequestDto) target;
        String path = pathRequestDto.getPath();

        if (path.startsWith("/")) {
            errors.rejectValue("path", "", "Path field must not start with a character '/'");
        }

        if (path.contains("//")) {
            errors.rejectValue("path", "", "Path field must not contain more than one character '/' in a row");
        }

    }
}
