package org.walkmanx21.spring.cloudstorage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.*;

@RestControllerAdvice
@Slf4j
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
        return new ErrorResponseDto("Родительская папка не существует");
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDirectoryToCreateExistException() {
        return new ErrorResponseDto("Файл/папка с таким именем уже существует");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleMaxUploadSizeExceededException() {
        log.info("Поймали MaxUploadSizeExceededException");
        return new ErrorResponseDto("Загружаемый размер файла/файлов превышает лимит в 4ГБ");
    }
}
