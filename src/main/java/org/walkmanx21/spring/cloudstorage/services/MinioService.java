package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.walkmanx21.spring.cloudstorage.exceptions.MinioServiceException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    protected boolean checkBucketExist(String bucket) {
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

    protected void createBucket(String bucket) {
        try {
            minioClient.makeBucket(MakeBucketArgs
                    .builder().bucket(bucket)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    protected void createDirectory(String bucket, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
            log.info("Директория {} создана", path);
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    protected boolean checkResourceExist(String bucket, String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(false)
                        .build())
                .iterator().hasNext();
    }

    protected List<Item> getListObjects(String bucket, String prefix, boolean recursive) {
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

    protected List <Item> removeObject(String bucket, String object) {
        List<Item> items = getListObjects(bucket, object, true);
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
        return items;
    }

    protected void uploadResources(String bucket, String destinationDirectory, Map<String, MultipartFile> files) {
        files.forEach((key, file) -> {
            try (InputStream inputStream = file.getInputStream()) {
                String fullFileName = destinationDirectory + file.getOriginalFilename();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullFileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            } catch (Exception e) {
                throw new MinioServiceException(e.getMessage(), e);
            }
        });
    }

    protected InputStream getObject(String bucket, String object) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }

    protected void copyObject(String bucket, String oldObject, String newObject) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(newObject)
                    .source(CopySource.builder()
                            .bucket(bucket)
                            .object(oldObject)
                            .build())
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException(e.getMessage(), e);
        }
    }
}
