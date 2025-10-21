package org.walkmanx21.spring.cloudstorage.controllers.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;

import java.util.List;

public interface DirectoryApi {

    @Operation(
            summary = "Вывести содержимое папки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Коллекция ресурсов, лежащих в папке"),
                    @ApiResponse(responseCode = "404", description = "Папка не существует")
            }
    )
    @GetMapping
    ResponseEntity<List<ResourceDto>> getDirectoryContents(
            @RequestParam @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path);

    @Operation(
            summary = "Создать пустую папку",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Папка создана"),
                    @ApiResponse(responseCode = "404", description = "Родительская папка не существует"),
                    @ApiResponse(responseCode = "409", description = "Папка уже существует")
            }
    )
    @PostMapping
    ResponseEntity<ResourceDto> createDirectory(
            @RequestParam @ValidPath String path);
}
