package org.walkmanx21.spring.cloudstorage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<Resource>> getDirectoryContents(
            @RequestParam @ValidPath String path) {
        return ResponseEntity.ok(storageService.getDirectoryContents(path));
    }

    @PostMapping
    public ResponseEntity<Resource> createDirectory(
            @RequestParam @ValidPath String path) {
        return ResponseEntity.ok(storageService.createDirectory(path));
    }
}
