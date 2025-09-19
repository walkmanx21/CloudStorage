package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.exceptions.DirectoryToCreateAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ParentDirectoryNotExistException;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;
import org.walkmanx21.spring.cloudstorage.util.ResourceBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioService minioService;
    private final ResourceBuilder resourceBuilder;
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
        return resourceBuilder.build(directory, fullPathToDirectory, 0L);
    }

    public void createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
    }

    public Resource getResourceData(PathRequestDto pathRequestDto) {
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        StatObjectResponse stat = minioService.getResourceData(ROOT_BUCKET, fullPath);
        return resourceBuilder.build(Paths.get(pathRequestDto.getPath()), fullPath, stat.size());
    }


    public List<Resource> getDirectoryContents(PathRequestDto pathRequestDto){
        Path path = Paths.get(pathRequestDto.getPath());
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        List<Item> items = minioService.getDirectoryContents(ROOT_BUCKET, fullPath, false);
        items.remove(0);
        List<Resource> resources = new ArrayList<>();
        for (Item item : items) {
            resources.add(resourceBuilder.build(path, item));
        }
        return resources;
    }

    public void removeResource(PathRequestDto pathRequestDto) {
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        minioService.removeObject(ROOT_BUCKET, fullPath);
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

    private void checkParentDirectoryExist(String parent) {
        boolean exists = minioService.checkDirectoryExist(ROOT_BUCKET, parent);
        if (!exists)
            throw new ParentDirectoryNotExistException();
    }

    private void checkDirectoryToCreateExist(String path) {
        boolean exists = minioService.checkDirectoryExist(ROOT_BUCKET, path);
        if (exists)
            throw new DirectoryToCreateAlreadyExistException();
    }


}
