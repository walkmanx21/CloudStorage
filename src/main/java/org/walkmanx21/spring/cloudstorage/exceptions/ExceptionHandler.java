package org.walkmanx21.spring.cloudstorage.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDataIntegrityViolationException() {
        return new ErrorResponseDto("Пользователь с данным именем уже существует");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleUsernameNotFoundException(Exception e) {
        return new ErrorResponseDto(e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({
            ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleErrorResponse() {
        return new ErrorResponseDto("Файл/папка не существует");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ParentDirectoryNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleParentDirectoryNotExistException() {
        return new ErrorResponseDto("Родительская папка не существует");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ResourceAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDirectoryToCreateExistException() {
        return new ErrorResponseDto("Файл/папка с таким именем уже существует");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleMaxUploadSizeExceededException() {
        return new ErrorResponseDto("Загружаемый размер файла/файлов превышает лимит в 4ГБ");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DownloadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleDownloadException() {
        return new ErrorResponseDto("Ошибка загрузки");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> errors = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return errorResponseDtoBuilder(errors);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleConstraintViolationException(ConstraintViolationException e) {
        return new ErrorResponseDto(e.getMessage());
    }

    private ErrorResponseDto errorResponseDtoBuilder(List<String> errors) {
        StringBuilder builder = new StringBuilder();
        errors.forEach(error -> builder.append(error).append("; "));
        return new ErrorResponseDto(builder.toString());
    }
}
