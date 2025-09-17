package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidRequestDataException;

import java.util.List;

@Component
public class InvalidRequestDataExceptionThrower {
    public void throwInvalidRequestDataException(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> builder.append(error.getDefaultMessage()).append("; "));
        throw new InvalidRequestDataException(builder.toString());
    }
}
