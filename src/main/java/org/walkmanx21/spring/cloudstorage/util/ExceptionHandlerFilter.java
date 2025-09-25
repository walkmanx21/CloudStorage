package org.walkmanx21.spring.cloudstorage.util;

import io.minio.errors.ErrorResponseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.walkmanx21.spring.cloudstorage.dto.ErrorResponseDto;
import org.walkmanx21.spring.cloudstorage.exceptions.*;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerFilter {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDataIntegrityViolationException() {
        return new ErrorResponseDto("Пользователь с данным именем уже существует");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleUsernameNotFoundException(Exception e) {
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler({ErrorResponseException.class, ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleErrorResponse() {
        return new ErrorResponseDto("Файл/папка не существует");
    }

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
        return new ErrorResponseDto("Загружаемый размер файла/файлов превышает лимит в 4ГБ");
    }

    @ExceptionHandler(DownloadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleDownloadException() {
        return new ErrorResponseDto("Ошибка загрузки");
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> errors = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return errorResponseDtoBuilder(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage).toList();
        return errorResponseDtoBuilder(errors);
    }

    private ErrorResponseDto errorResponseDtoBuilder(List<String> errors) {
        StringBuilder builder = new StringBuilder();
        errors.forEach(error -> builder.append(error).append("; "));
        return new ErrorResponseDto(builder.toString());
    }
}
