package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.exceptions.DirectoryToCreateAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ParentDirectoryNotExistException;
import org.walkmanx21.spring.cloudstorage.models.File;
import org.walkmanx21.spring.cloudstorage.models.Directory;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioService minioService;
    private static final String ROOT_BUCKET = "user-files";

    @PostConstruct
    public void init() {
        if (!checkRootBucketExist()) {
            createRootBucket();
        }
    }

    public Resource createDirectory(PathRequestDto pathRequestDto) {
        Path path = Paths.get(pathRequestDto.getPath());

        String parent;
        if (path.getParent() == null)
            parent = getUserRootDirectory();
        else
            parent = path.getParent() + "/";

        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();

        checkParentDirectoryExist(parent);
        if (!checkDirectoryToCreateExist(pathRequestDto.getPath())) {
            minioService.createDirectory(ROOT_BUCKET, fullPath);
            return resourceBuilder(pathRequestDto, fullPath, 0L);
        } else {
            throw new DirectoryToCreateAlreadyExistException();
        }
    }

    public void createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
    }

    public Resource getResourceData(PathRequestDto pathRequestDto) {
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        StatObjectResponse stat = minioService.getResourceData(ROOT_BUCKET, fullPath);
        return resourceBuilder(pathRequestDto, fullPath, stat.size());
    }

    private void createRootBucket() {
        minioService.createBucket(ROOT_BUCKET);
    }

    private boolean checkRootBucketExist() {
        return minioService.checkBucketExist(ROOT_BUCKET);
    }

    private String getUserRootDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "user-" + myUserDetails.getUser().getId() + "-files/";
    }

    private Resource resourceBuilder(PathRequestDto pathRequestDto, String fullPath, Long size) {
        String requestDtoPath = pathRequestDto.getPath();
        Path path = Paths.get(requestDtoPath);

        String parent;
        if (path.getParent() == null)
            parent = "/";
        else
            parent = path.getParent() + "/";

        if (fullPath.endsWith("/")) {
            return Directory.builder()
                    .path(parent)
                    .type(ResourceType.DIRECTORY)
                    .name(path.getFileName().toString())
                    .build();
        } else {
            return File.builder()
                    .path(parent)
                    .type(ResourceType.FILE)
                    .name(path.getFileName().toString())
                    .size(size)
                    .build();
        }
    }

    private void checkParentDirectoryExist(String parent) {
        try {
            minioService.getResourceData(ROOT_BUCKET, parent);
        } catch (Exception e) {
            throw new ParentDirectoryNotExistException();
        }
    }

    private boolean checkDirectoryToCreateExist(String path) {
        log.info("Зашли в проверку существования добавляемой папки");
        try {
            minioService.getResourceData(ROOT_BUCKET, path);
            log.info("Добавляемая папка существует");
            return true;
        } catch (Exception e) {
            log.info("Добавляемая папка не существует");
            return false;
        }
    }


}
