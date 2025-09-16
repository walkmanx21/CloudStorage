package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.exceptions.MinioServiceException;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @PostConstruct
    public void init() {
        createStartBucket();
    }

    public void createDirectory(String path) {
        createUserDirectory();
    }

    private void createStartBucket() {
        try {
            minioClient.makeBucket(MakeBucketArgs
                    .builder().bucket("user-files")
                    .build());
            log.info("Стартовый bucket успешно создан");
        } catch (Exception e) {
            log.info("Стартовый bucket ранее уже был создан");
        }
    }

    private void createUserDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int userId = myUserDetails.getUser().getId();
        String userDirectory = "user-" + userId + "-files/";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("user-files")
                    .object(userDirectory)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
            log.info("Рабочая директория пользователя {} успешно создана", myUserDetails.getUser().getUsername());
        } catch (Exception e) {
            log.info("Рабочая директория пользователя {} уже создана", myUserDetails.getUser().getUsername());
        }
    }
}
