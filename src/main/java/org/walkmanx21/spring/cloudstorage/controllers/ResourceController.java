package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidPathException;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.util.InvalidRequestDataExceptionThrower;
import org.walkmanx21.spring.cloudstorage.util.PathValidator;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
@Slf4j
public class ResourceController {

    private final StorageService storageService;
    private final PathValidator pathValidator;
    private final InvalidRequestDataExceptionThrower exceptionThrower;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(pathValidator);
    }

    @GetMapping
    public ResponseEntity<Resource> showResourceData(@ModelAttribute @Valid PathRequestDto pathRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
//            throwInvalidPathException(bindingResult);
            exceptionThrower.throwInvalidRequestDataException(bindingResult);
        }
        return new ResponseEntity<>(storageService.getResourceData(pathRequestDto), HttpStatus.OK);
    }

//    private void throwInvalidPathException(BindingResult bindingResult) {
//        StringBuilder builder = new StringBuilder();
//        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//        fieldErrors.forEach(error -> builder.append(error.getDefaultMessage()).append("; "));
//        throw new InvalidPathException(builder.toString());
//    }

}
