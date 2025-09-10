package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.BadCredentialsException;

@RestControllerAdvice
public class ExceptionHandlerFilter {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleBadCredentials(Exception e) {
        return new ErrorResponseDto(e.getMessage());
    }
}
