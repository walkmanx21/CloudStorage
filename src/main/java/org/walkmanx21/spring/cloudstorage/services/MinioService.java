package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.exceptions.MinioServiceException;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @PostConstruct
    public void init() {
        if (!checkRootBucketExist()) {
            createRootBucket();
        }
    }

//    public void createDirectory(String path) {
//        createUserDirectory();
//    }



    public void createUserDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("user-files")
                    .object(userDirectory)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
            log.info("Рабочая директория пользователя c id={} успешно создана", userId);
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    private void createRootBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs
                    .builder().bucket("user-files")
                    .build());
            log.info("Стартовый bucket успешно создан");
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    private boolean checkRootBucketExist() {
        boolean exist;
        try {
            exist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket("user-files")
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
        return exist;
    }
}
