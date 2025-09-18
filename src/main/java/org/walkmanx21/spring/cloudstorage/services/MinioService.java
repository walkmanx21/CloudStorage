package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.exceptions.MinioServiceException;
import org.walkmanx21.spring.cloudstorage.models.Directory;
import org.walkmanx21.spring.cloudstorage.models.File;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;

import java.io.ByteArrayInputStream;


@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    public boolean checkBucketExist(String bucket) {
        boolean exist;
        try {
            exist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
        return exist;
    }

    public void createBucket(String bucket) {
        try {
            minioClient.makeBucket(MakeBucketArgs
                    .builder().bucket(bucket)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    public void createDirectory(String bucket, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    public StatObjectResponse getResourceData(String bucket, String path) {
        StatObjectResponse stat;
        try {
            stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(path).build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
        return stat;
    }

    public boolean getListObjects(String bucket, String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(false)
                .build())
                .iterator().hasNext();
    }

}
