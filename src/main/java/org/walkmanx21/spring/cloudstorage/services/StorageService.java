package org.walkmanx21.spring.cloudstorage.services;

import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.exceptions.DownloadException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ParentDirectoryNotExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;
import org.walkmanx21.spring.cloudstorage.util.ResourceBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioService minioService;
    private final ResourceBuilder resourceBuilder;
    private static final String ROOT_BUCKET = "user-files";

    @PostConstruct
    public void init() {
        if (!minioService.checkBucketExist(ROOT_BUCKET)) {
            minioService.createBucket(ROOT_BUCKET);
        }
    }

    public Resource createDirectory(String path) {
        Path object = Paths.get(path);
        String fullObject = getFullObject(path);
        String parent = object.getParent() == null ? getUserRootDirectory() :  getUserRootDirectory() + object.getParent().toString().replace("\\", "/") + "/";

        boolean parentDirectoryExist = minioService.checkResourceExist(ROOT_BUCKET, parent);
        boolean directoryToCreateExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject);

        if(!parentDirectoryExist)
            throw new ParentDirectoryNotExistException();

        if(directoryToCreateExist)
            throw new ResourceAlreadyExistException();

        minioService.createDirectory(ROOT_BUCKET, fullObject);
        return resourceBuilder.build(object, fullObject, 0L);
    }

    public void createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
    }

    public Resource getResourceData(String path) {
        String fullObject = getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        Item item;
        if (!items.isEmpty())
            item = items.get(0);
        else
            throw new ResourceNotFoundException();
        return resourceBuilder.build(path, item);
    }


    public List<Resource> getDirectoryContents(String path){
        String fullObject = getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        List<Resource> resources = new ArrayList<>();
        for (Item item : items) {
            if (!item.objectName().equals(fullObject))
                resources.add(resourceBuilder.build(item));
        }
        return resources;
    }

    public void removeResource(String object) {
        String fullPath = getFullObject(object);
        minioService.removeObject(ROOT_BUCKET, fullPath);
    }

    public List<Resource> uploadResources(String path, List<MultipartFile> files) {
        String fullObject = getFullObject(path);

        Map<String, MultipartFile> filesMap = new HashMap<>();
        for(MultipartFile file : files) {
            boolean fileExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject + file.getOriginalFilename());

            if (!fileExist)
                filesMap.put(file.getOriginalFilename(), file);
            else
                throw new ResourceAlreadyExistException();
        }

        minioService.uploadResources(ROOT_BUCKET, fullObject, filesMap);
        List<Resource> resources = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            Path object = Paths.get(path + file.getOriginalFilename());
            resources.add(resourceBuilder.buildFile(object, file.getSize()));
        });

        return resources;
    }

    public StreamingResponseBody downloadResource(String requestObject) {
        String userDirectory = getUserRootDirectory();
        String fullPath = getFullObject(requestObject);

        boolean resourceExist = minioService.checkResourceExist(ROOT_BUCKET, fullPath);
        if (!resourceExist)
            throw new ResourceNotFoundException();

        return outputStream -> {
            if (fullPath.endsWith("/")) {
                downloadDirectory(outputStream, fullPath, userDirectory);
            } else {
                downloadFile(outputStream, fullPath);
            }
        };
    }

    public Resource moveOrRenameResource(String from, String to) {
        String oldObject = getFullObject(from);
        String newObject = getFullObject(to);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, oldObject, true);
        items.forEach(item -> {
            minioService.copyObject(ROOT_BUCKET, item.objectName(), newObject);
            minioService.removeObject(ROOT_BUCKET, item.objectName());
        });
        Item item = minioService.getListObjects(ROOT_BUCKET, newObject, false).get(0);
        return resourceBuilder.build(to, item);
    }

    private String getUserRootDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "user-" + myUserDetails.getUser().getId() + "-files/";
    }

    private String getFullObject(String requestObject) {
        if (requestObject.contains(getUserRootDirectory()))
            return requestObject;
        else
            return  getUserRootDirectory() + requestObject;
    }

    private void downloadDirectory(OutputStream outputStream, String fullPath, String userDirectory) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            List<Item> directoryContents = minioService.getListObjects(ROOT_BUCKET, fullPath, true);
            directoryContents.forEach(object -> {
                if (!object.isDir()) {
                    String entryName = object.objectName().substring(userDirectory.length());
                    try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, object.objectName())) {
                        zipOutputStream.putNextEntry(new ZipEntry(entryName));
                        inputStream.transferTo(zipOutputStream);
                        zipOutputStream.closeEntry();
                    } catch (IOException e) {
                        throw new DownloadException();
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private void downloadFile(OutputStream outputStream, String fullPath) {
        try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, fullPath)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new DownloadException();
        }
    }


}
