package org.walkmanx21.spring.cloudstorage.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.controllers.api.ResourceApi;
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
public class ResourceController implements ResourceApi {

    private final StorageService storageService;
    private final SearchService searchService;

    @Override
    public ResponseEntity<ResourceDto> showResourceData(@RequestParam @ValidPath String path) {
        return ResponseEntity.ok(storageService.getResourceData(path));
    }

    @Override
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

    @Override
    public ResponseEntity<ResourceDto> moveOrRenameResource(@RequestParam @ValidPath String from, @RequestParam @ValidPath String to) {
        return ResponseEntity.ok(storageService.moveOrRenameResource(from, to));
    }

    @Override
    public ResponseEntity<Set<ResourceDto>> searchResources(@RequestParam @NotBlank String query) {
        return ResponseEntity.ok(searchService.searchResources(query));
    }

    @Override
    public ResponseEntity<List<ResourceDto>> uploadResources(
            @RequestParam @Size(max = 1000, message = "Поле path должно быть не более 1000 символов") String path,
            @RequestPart("object") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.uploadResources(path, files));
    }

    @Override
    public ResponseEntity<Void> deleteResource(@RequestParam @ValidPath String path) {
        storageService.removeResource(path);
        return ResponseEntity.noContent().build();
    }

}
