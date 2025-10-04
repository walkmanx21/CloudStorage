package org.walkmanx21.spring.cloudstorage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.dto.DownloadResponseDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.services.SearchService;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
@Validated
@Slf4j
public class ResourceController {

    private final StorageService storageService;
    private final SearchService searchService;

    @Operation(
            summary = "Получить информацию о ресурсе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON с информацией о ресурсе"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @GetMapping
    public ResponseEntity<ResourceDto> showResourceData(@RequestParam @ValidPath String path) {
        return ResponseEntity.ok(storageService.getResourceData(path));
    }

    @Operation(
            summary = "Скачать ресурс",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Бинарное содержимое файла"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam @ValidPath String path) {
        DownloadResponseDto dto = storageService.downloadResource(path);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(dto.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header(HttpHeaders.CONNECTION, "close")
                .body(dto.getStreamingResponseBody());
    }

    @Operation(
            summary = "Переименовать или переместить ресурс",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON с конечным ресурсом"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
                    @ApiResponse(responseCode = "409", description = "Ресурс лежащий по пути to уже существует"),
            }
    )
    @GetMapping("/move")
    public ResponseEntity<ResourceDto> moveOrRenameResource(@RequestParam @ValidPath String from, @RequestParam @ValidPath String to) {
        return ResponseEntity.ok(storageService.moveOrRenameResource(from, to));
    }

    @Operation(
            summary = "Поиск ресурса",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON с найденным ресурсом")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Set<ResourceDto>> searchResources(@RequestParam @NotBlank String query) {
        return ResponseEntity.ok(searchService.searchResources(query));
    }

    @Operation(
            summary = "Upload ресурса",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Коллекция загруженных ресурсов"),
                    @ApiResponse(responseCode = "409", description = "Файл уже существует")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceDto>> uploadResources(
            @RequestParam @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path,
            @RequestPart("object") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.uploadResources(path, files));
    }

    @Operation(
            summary = "Удаление ресурса",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Тело ответа пустое"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam @ValidPath String path) {
        storageService.removeResource(path);
        return ResponseEntity.noContent().build();
    }

}
