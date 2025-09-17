package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidPathException;
import org.walkmanx21.spring.cloudstorage.models.Folder;
import org.walkmanx21.spring.cloudstorage.util.InvalidRequestDataExceptionThrower;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final InvalidRequestDataExceptionThrower exceptionThrower;

    @PostMapping
    public ResponseEntity<Folder> createFolder(@ModelAttribute @Valid PathRequestDto pathRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            exceptionThrower.throwInvalidRequestDataException(bindingResult);
        }
    }
}
