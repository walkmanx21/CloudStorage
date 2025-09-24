package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PathValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PathRequestDto pathRequestDto = (PathRequestDto) target;

        if (pathRequestDto.getPath() == null) {
            errors.rejectValue("path", "", "Невалидный или отсутствующий путь");
            return;
        }

        Path path = Paths.get(pathRequestDto.getPath());

//        if (path.startsWith("/")) {
//            errors.rejectValue("path", "", "Path field must not start with a character '/'");
//        }

        if (path.toString().contains("//")) {
            errors.rejectValue("path", "", "Поле path не должно содержать более одного символа \"/\" подряд");
        }

    }
}
