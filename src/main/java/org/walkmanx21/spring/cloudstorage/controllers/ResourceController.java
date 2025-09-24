package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.services.StorageService;
import org.walkmanx21.spring.cloudstorage.util.PathValidator;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
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
    public ResponseEntity<InputStreamResource> downloadResource(@ModelAttribute @Valid PathRequestDto pathRequestDto) {
        String fileName = Paths.get(pathRequestDto.getPath()).getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(storageService.downloadResource(pathRequestDto)));
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
