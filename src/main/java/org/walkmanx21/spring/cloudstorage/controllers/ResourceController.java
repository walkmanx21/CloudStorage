package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.validation.ValidPath;

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

    @GetMapping
    public ResponseEntity<Resource> showResourceData(@RequestParam @ValidPath String path) {
        return ResponseEntity.ok(storageService.getResourceData(path));
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam @ValidPath String path) {
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
    public ResponseEntity<Resource> moveOrRenameResource(@RequestParam @ValidPath String from, @RequestParam @ValidPath String to) {
        return ResponseEntity.ok(storageService.moveOrRenameResource(from, to));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Resource>> searchResources(@RequestParam @NotBlank String query) {
        return ResponseEntity.ok(storageService.searchResources(query));
    }

    @PostMapping
    public ResponseEntity<List<Resource>> uploadResources(
            @RequestParam @Size(max = 1024, message = "Поле from должно быть не более 1024 символов") String path,
            @RequestParam("object") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.uploadResources(path, files));
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam @ValidPath String path) {
        storageService.removeResource(path);
        return ResponseEntity.noContent().build();
    }

}
