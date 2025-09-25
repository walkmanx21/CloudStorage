package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
    public ResponseEntity<List<Resource>> getDirectoryContents(
            @RequestParam @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path) {
        return ResponseEntity.ok(storageService.getDirectoryContents(path));
    }

    @PostMapping
    public ResponseEntity<Resource> createDirectory(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        return ResponseEntity.ok(storageService.createDirectory(pathRequestDto));
    }
}
