package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidCredentialsException;
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

//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ErrorResponseDto handleUnknownError() {
//        return new ErrorResponseDto("Something went wrong. Try again later");
//    }

}
