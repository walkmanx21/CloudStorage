package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.exceptions.MinioServiceException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;


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

    public boolean checkDirectoryExist(String bucket, String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(false)
                .build())
                .iterator().hasNext();
    }

    public List<Item> getDirectoryContents(String bucket, String prefix, boolean recursive) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(recursive)
                .build());

        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
        return items;
    }

    public void removeObject(String bucket, String object) {
        List<Item> items = getDirectoryContents(bucket, object, true);
        if (items.isEmpty())
            throw new ResourceNotFoundException();
        for (Item item : items) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(item.objectName())
                        .build());
            } catch (Exception e) {
                throw new MinioServiceException(e.getMessage(), e);
            }
        }
    }

}
