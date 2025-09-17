package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
import org.walkmanx21.spring.cloudstorage.exceptions.MinioServiceException;
import org.walkmanx21.spring.cloudstorage.models.File;
import org.walkmanx21.spring.cloudstorage.models.Folder;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;
    private static final String ROOT_BUCKET = "user-files";

    @PostConstruct
    public void init() {
        if (!checkRootBucketExist()) {
            createRootBucket();
        }
    }

    public void createUserDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(ROOT_BUCKET)
                    .object(userDirectory)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    public Resource getResourceData(PathRequestDto pathRequestDto) {

        String path =  getUserRootDirectory() + pathRequestDto.getPath();
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(ROOT_BUCKET)
                    .object(path).build());

            if (path.endsWith("/")) {
                return Folder.builder()
                        .path(pathRequestDto.getPath())
                        .type(ResourceType.DIRECTORY)
                        .name(Paths.get(path).getFileName().toString())
                        .build();
            } else {
                return File.builder()
                        .path(pathRequestDto.getPath())
                        .type(ResourceType.FILE)
                        .name(Paths.get(path).getFileName().toString())
                        .size(stat.size())
                        .build();
            }

        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    private void createRootBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs
                    .builder().bucket(ROOT_BUCKET)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    private boolean checkRootBucketExist() {
        boolean exist;
        try {
            exist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(ROOT_BUCKET)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
        return exist;
    }

    private String getUserRootDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "user-" + myUserDetails.getUser().getId() + "-files/";
    }
}
