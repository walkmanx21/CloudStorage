package org.walkmanx21.spring.cloudstorage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.walkmanx21.spring.cloudstorage.dto.DirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final StorageService storageService;

    @Operation(
            summary = "Вывести содержимое папки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Коллекция ресурсов, лежащих в папке"),
                    @ApiResponse(responseCode = "404", description = "Папка не существует")
            }
    )
    @GetMapping
    public ResponseEntity<List<ResourceDto>> getDirectoryContents(
            @RequestParam @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path) {
        return ResponseEntity.ok(storageService.getDirectoryContents(path));
    }

    @Operation(
            summary = "Создать пустую папку",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Папка создана"),
                    @ApiResponse(responseCode = "404", description = "Родительская папка не существует"),
                    @ApiResponse(responseCode = "409", description = "Папка уже существует")
            }
    )
    @PostMapping
    public ResponseEntity<DirectoryDto> createDirectory(
            @RequestParam @ValidPath String path) {
        return ResponseEntity.ok(storageService.createDirectory(path));
    }
}
