package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.controllers.api.DirectoryApi;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController implements DirectoryApi {

    private final StorageService storageService;

    @Override
    public ResponseEntity<List<ResourceDto>> getDirectoryContents(
            @RequestParam @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path) {
        return ResponseEntity.ok(storageService.getDirectoryContents(path));
    }

    @Override
    public ResponseEntity<ResourceDto> createDirectory(
            @RequestParam @ValidPath String path) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.createDirectory(path));
    }
}
