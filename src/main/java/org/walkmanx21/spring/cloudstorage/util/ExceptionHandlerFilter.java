package org.walkmanx21.spring.cloudstorage.util;

import io.minio.errors.ErrorResponseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.*;

import java.io.IOException;

@RestControllerAdvice
public class ExceptionHandlerFilter {

    @ExceptionHandler(InvalidRequestDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleInvalidDataException(Exception e) {
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

//    @ExceptionHandler({ErrorResponseException.class, ResourceNotFoundException.class})
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ErrorResponseDto handleErrorResponse() {
//        return new ErrorResponseDto("The resource was not found on the specified path");
//    }

    @ExceptionHandler(ParentDirectoryNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleParentDirectoryNotExistException() {
        return new ErrorResponseDto("The parent folder does not exist");
    }

    @ExceptionHandler(DirectoryToCreateAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDirectoryToCreateExistException() {
        return new ErrorResponseDto("Directory to create already exist");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handeIOException() {
        return new ErrorResponseDto("Upload failure");
    }
}
