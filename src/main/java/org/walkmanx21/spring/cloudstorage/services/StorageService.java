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
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;
import org.walkmanx21.spring.cloudstorage.util.ResourceDtoBuilder;

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
    private final ResourceDtoBuilder resourceDtoBuilder;
    private final SearchService searchService;
    private static final String ROOT_BUCKET = "user-files";

    @PostConstruct
    public void init() {
        if (!minioService.checkBucketExist(ROOT_BUCKET)) {
            minioService.createBucket(ROOT_BUCKET);
        }
    }

    public void createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
    }

    public ResourceDto createDirectory(String path) {
        String fullObject = getFullObject(path);
        String parent = getParent(path);

        boolean parentDirectoryExist = minioService.checkResourceExist(ROOT_BUCKET, parent);
        boolean directoryToCreateExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject);

        if(!parentDirectoryExist)
            throw new ParentDirectoryNotExistException();

        if(directoryToCreateExist)
            throw new ResourceAlreadyExistException();

        minioService.createDirectory(ROOT_BUCKET, fullObject);
        searchService.writeUserResourceToDatabase(fullObject);

        return resourceDtoBuilder.buildDirectoryDto(path);
    }

    public ResourceDto getResourceData(String path) {
        String fullObject = getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        Item item;
        if (!items.isEmpty())
            item = items.get(0);
        else
            throw new ResourceNotFoundException();
        return resourceDtoBuilder.build(path, item);
    }

    public List<ResourceDto> getDirectoryContents(String path){
        String fullObject = getFullObject(path);
        String userRootDirectory = getUserRootDirectory();
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        List<ResourceDto> resources = new ArrayList<>();
        for (Item item : items) {
            if (!item.objectName().equals(fullObject)) {
                String object = item.objectName().substring(userRootDirectory.length());
                resources.add(resourceDtoBuilder.build(object, item));
            }
        }
        return resources;
    }

    public void removeResource(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        String fullObject = getFullObject(path);
        List<Item> deletedItems = minioService.removeObject(ROOT_BUCKET, fullObject);
        deletedItems.forEach(item -> searchService.removeUserResourceFromDatabase(item.objectName()));
        searchService.removeUserResourceFromDatabase(path);
    }

    public List<ResourceDto> uploadResources(String path, List<MultipartFile> files) {
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
        List<ResourceDto> resources = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            String object = path + file.getOriginalFilename();
            resources.add(resourceDtoBuilder.buildFileDto(object, file.getSize()));
            searchService.writeUserResourceToDatabase(getFullObject(object));
        });

        return resources;
    }

    public StreamingResponseBody downloadResource(String requestObject) {
        String userDirectory = getUserRootDirectory();
        String fullObject = getFullObject(requestObject);

        boolean resourceExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject);
        if (!resourceExist)
            throw new ResourceNotFoundException();

        return outputStream -> {
            if (fullObject.endsWith("/")) {
                downloadDirectory(outputStream, fullObject, userDirectory);
            } else {
                downloadFile(outputStream, fullObject);
            }
        };
    }

    public ResourceDto moveOrRenameResource(String from, String to) {
        if (from.startsWith("/"))
            from = from.substring(1);

        if (to.startsWith("/"))
            to = to.substring(1);

        String oldObject = getFullObject(from);
        String newObject = getFullObject(to);
        String parentOfNewObject = getParent(newObject);

        boolean oldObjectExist = minioService.checkResourceExist(ROOT_BUCKET, oldObject);
        boolean parentOfNewObjectExist = minioService.checkResourceExist(ROOT_BUCKET, parentOfNewObject);
        if (!oldObjectExist || !parentOfNewObjectExist)
            throw new ResourceNotFoundException();

        boolean newFileAlreadyExist = minioService.checkResourceExist(ROOT_BUCKET, newObject);
        if (newFileAlreadyExist)
            throw new ResourceAlreadyExistException();

        List<Item> items = minioService.getListObjects(ROOT_BUCKET, oldObject, true);
        items.forEach(item -> {
            minioService.copyObject(ROOT_BUCKET, item.objectName(), newObject);
            List<Item> newItems = minioService.getListObjects(ROOT_BUCKET, newObject, true);
            newItems.forEach(newItem -> searchService.writeUserResourceToDatabase(newItem.objectName()));

            List<Item> oldItems = minioService.removeObject(ROOT_BUCKET, item.objectName());
            oldItems.forEach(oldItem -> searchService.removeUserResourceFromDatabase(oldItem.objectName()));

        });
        Item item = minioService.getListObjects(ROOT_BUCKET, newObject, false).get(0);
        return resourceDtoBuilder.build(to, item);
    }

    public List<ResourceDto> searchResources(String query) {
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, getUserRootDirectory(), true);
        items.remove(0);
        List<ResourceDto> foundResources = new ArrayList<>();
        items.forEach(item -> {
            Path path = Paths.get(item.objectName());
            String fileName = path.getFileName().toString();
            if (fileName.contains(query)) {
                String object = item.objectName().substring(getUserRootDirectory().length());
                foundResources.add(resourceDtoBuilder.build(object, item));
            }
        });
        return foundResources;
    }

    private String getUserRootDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "user-" + myUserDetails.getUser().getId() + "-files/";
    }

    private String getFullObject(String requestObject) {
        if (requestObject.startsWith(getUserRootDirectory()))
            return requestObject;
        else
            return  (getUserRootDirectory() + requestObject).replace("//", "/");
    }

    private String getParent(String path) {
        Path object = Paths.get(path);
        Path parent = object.getParent();
        if (path.startsWith(getUserRootDirectory()))
            return parent.toString().replace("\\", "/") + "/";
        else
            return parent == null ? getUserRootDirectory() : getUserRootDirectory() + parent.toString().replace("\\", "/") + "/";
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
            throw new DownloadException();
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
