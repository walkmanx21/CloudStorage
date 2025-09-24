package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<Resource>> getDirectoryContents(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        return new ResponseEntity<>(storageService.getDirectoryContents(pathRequestDto), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Resource> createDirectory(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        return new ResponseEntity<>(storageService.createDirectory(pathRequestDto), HttpStatus.OK);
    }
}
