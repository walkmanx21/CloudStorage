package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.util.PathValidator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
@Validated
@Slf4j
public class ResourceController {

    private final StorageService storageService;
    private final PathValidator pathValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(pathValidator);
    }

    @GetMapping
    public ResponseEntity<Resource> showResourceData(@RequestParam @NotBlank @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path) {
        return ResponseEntity.ok(storageService.getResourceData(path));
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam @NotBlank @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path) {
        String resourceName = Paths.get(path).getFileName().toString();
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(resourceName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(storageService.downloadResource(path));
    }

    @GetMapping("/move")
    public ResponseEntity<Resource> moveOrRenameResource(
            @RequestParam @NotBlank @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String from,
            @RequestParam @NotBlank @Size(max = 1024, message = "Поле to должно быть не более 1024 символов") String to
    ) {
        return ResponseEntity.ok(storageService.moveOrRenameResource(from, to));
    }

    @PostMapping
    public ResponseEntity<List<Resource>> uploadResources(@ModelAttribute @Valid PathRequestDto pathRequestDto, @RequestParam("object") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.uploadResources(pathRequestDto, files));
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        storageService.removeResource(pathRequestDto);
        return ResponseEntity.noContent().build();
    }

}
