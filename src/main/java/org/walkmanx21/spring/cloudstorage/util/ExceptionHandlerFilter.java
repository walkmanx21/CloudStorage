package org.walkmanx21.spring.cloudstorage.util;

import io.minio.errors.ErrorResponseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidCredentialsException;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidPathException;
import org.walkmanx21.spring.cloudstorage.exceptions.UserUnauthorizedException;

@RestControllerAdvice
public class ExceptionHandlerFilter {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidCredentials(Exception e) {
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDataIntegrityViolationException() {
        return new ErrorResponseDto("User with this username already exist");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleUsernameNotFoundException(Exception e) {
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler(UserUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleUsernameNotFoundException() {
    }

    @ExceptionHandler(InvalidPathException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidPath(Exception e) {
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler(ErrorResponseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleErrorResponse() {
        return new ErrorResponseDto("The resource was not found on the specified path");
    }

}
