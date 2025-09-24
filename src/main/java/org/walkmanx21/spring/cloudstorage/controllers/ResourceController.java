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
    public ResponseEntity<Resource> showResourceData(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        return new ResponseEntity<>(storageService.getResourceData(pathRequestDto), HttpStatus.OK);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        String resourceName = Paths.get(pathRequestDto.getPath()).getFileName().toString();
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(resourceName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(storageService.downloadResource(pathRequestDto));
    }

    @GetMapping("/move")
    public ResponseEntity<Resource> moveOrRenameResource(
            @RequestParam @NotBlank @Size(max = 10, message = "Поле path должно быть не более 10 символов") String from,
            @RequestParam @NotBlank @Size(max = 1024, message = "Path field must not be longer than 1024 characters.") String to
    ) {
        return null;
    }

    @PostMapping
    public ResponseEntity<List<Resource>> uploadResources(@ModelAttribute @Valid PathRequestDto pathRequestDto, @RequestParam("object") List<MultipartFile> files) {
        return new ResponseEntity<>(storageService.uploadResources(pathRequestDto, files), HttpStatus.CREATED);
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        storageService.removeResource(pathRequestDto);
        return ResponseEntity.noContent().build();
    }

}
