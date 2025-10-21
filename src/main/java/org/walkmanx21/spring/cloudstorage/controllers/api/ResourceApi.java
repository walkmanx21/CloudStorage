package org.walkmanx21.spring.cloudstorage.controllers.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;
import java.util.List;
import java.util.Set;

public interface ResourceApi {

    @Operation(
            summary = "Получить информацию о ресурсе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON с информацией о ресурсе"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @GetMapping
    ResponseEntity<ResourceDto> showResourceData(@RequestParam @ValidPath String path);

    @Operation(
            summary = "Скачать ресурс",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Бинарное содержимое файла"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @GetMapping("/download")
    ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam @ValidPath String path);

    @Operation(
            summary = "Переименовать или переместить ресурс",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON с конечным ресурсом"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
                    @ApiResponse(responseCode = "409", description = "Ресурс лежащий по пути to уже существует"),
            }
    )
    @GetMapping("/move")
    ResponseEntity<ResourceDto> moveOrRenameResource(@RequestParam @ValidPath String from, @RequestParam @ValidPath String to);

    @Operation(
            summary = "Поиск ресурса",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON с найденным ресурсом")
            }
    )
    @GetMapping("/search")
    ResponseEntity<Set<ResourceDto>> searchResources(@RequestParam @NotBlank String query);

    @Operation(
            summary = "Upload ресурса",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Коллекция загруженных ресурсов"),
                    @ApiResponse(responseCode = "409", description = "Файл уже существует")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<ResourceDto>> uploadResources(
            @RequestParam @Size(max = 1000, message = "Поле path должно быть не более 1000 символов") String path,
            @RequestPart("object") List<MultipartFile> files);

    @Operation(
            summary = "Удаление ресурса",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Тело ответа пустое"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @DeleteMapping
    ResponseEntity<Void> deleteResource(@RequestParam @ValidPath String path);
}
