package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.models.Directory;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.util.InvalidRequestDataExceptionThrower;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final InvalidRequestDataExceptionThrower exceptionThrower;
    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<Resource> createDirectory(@ModelAttribute @Valid PathRequestDto pathRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            exceptionThrower.throwInvalidRequestDataException(bindingResult);
        }
        return new ResponseEntity<>(storageService.createDirectory(pathRequestDto), HttpStatus.OK);
    }
}
