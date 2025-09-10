package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDataIntegrityViolationException() {
        return new ErrorResponseDto("User with this username already exist");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleUnknownError() {
        return new ErrorResponseDto("Something went wrong. Try again later");
    }

}
