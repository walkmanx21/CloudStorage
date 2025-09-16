package org.walkmanx21.spring.cloudstorage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.walkmanx21.spring.cloudstorage.services.MinioService;

@RestController
@RequiredArgsConstructor
public class MinioController {

    private final MinioService minioService;

    @PostMapping("/directory")
    public ResponseEntity<String> createDirectory(@RequestParam String path) {
        minioService.createDirectory(path);
        String str = "Hello from controller!";
        return new ResponseEntity<>(str, HttpStatus.CREATED);
    }
}
