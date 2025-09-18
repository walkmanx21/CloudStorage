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
        Path directory = Paths.get(pathRequestDto.getPath());
        String parent = directory.getParent() == null ? getUserRootDirectory() :  getUserRootDirectory() + directory.getParent().toString().replace("\\", "/") + "/";
        String fullPathToDirectory = getUserRootDirectory() + directory + "/";

        checkParentDirectoryExist(parent);
        checkDirectoryToCreateExist(fullPathToDirectory);
        minioService.createDirectory(ROOT_BUCKET, fullPathToDirectory.replace("\\", "/"));
        return resourceBuilder(directory, fullPathToDirectory, 0L);
    }

    public void createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
    }

    public Resource getResourceData(PathRequestDto pathRequestDto) {
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        StatObjectResponse stat = minioService.getResourceData(ROOT_BUCKET, fullPath);
        return resourceBuilder(Paths.get(pathRequestDto.getPath()), fullPath, stat.size());
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

    private Resource resourceBuilder(Path path, String fullPath, Long size) {
        String parent = path.getParent() == null ? "/" : path.getParent() + "/";
        if (fullPath.endsWith("/")) {
            return Directory.builder()
                    .path(parent.replace("\\", "/"))
                    .type(ResourceType.DIRECTORY)
                    .name(path.getFileName().toString())
                    .build();
        } else {
            return File.builder()
                    .path(parent.replace("\\", "/"))
                    .type(ResourceType.FILE)
                    .name(path.getFileName().toString())
                    .size(size)
                    .build();
        }
    }

    private void checkParentDirectoryExist(String parent) {
        boolean exists = minioService.getListObjects(ROOT_BUCKET, parent);
        if (!exists)
            throw new ParentDirectoryNotExistException();
    }

    private void checkDirectoryToCreateExist(String path) {
        boolean exists = minioService.getListObjects(ROOT_BUCKET, path);
        if (exists)
            throw new DirectoryToCreateAlreadyExistException();
    }


}
