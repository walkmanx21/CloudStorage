package org.walkmanx21.spring.cloudstorage.services;

import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.dto.DirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.exceptions.DownloadException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ParentDirectoryNotExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.util.PathUtil;
import org.walkmanx21.spring.cloudstorage.util.ResourceBuilder;
import org.walkmanx21.spring.cloudstorage.util.ResourceMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioService minioService;
    private final SearchService searchService;
    private final ResourceMapper resourceMapper;
    private final ResourceBuilder resourceBuilder;
    private final UserContextService userContextService;
    private final PathUtil pathUtil;

    private static final String ROOT_BUCKET = "user-files";

    @PostConstruct
    public void init() {
        if (!minioService.checkBucketExist(ROOT_BUCKET)) {
            minioService.createBucket(ROOT_BUCKET);
        }
    }

    public String createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
        return userDirectory;
    }

    public DirectoryDto createDirectory(String path) {
        String fullObject = pathUtil.getFullObject(path);
        String parent = pathUtil.getParent(path);

        boolean parentDirectoryExist = minioService.checkResourceExist(ROOT_BUCKET, parent);
        boolean directoryToCreateExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject);

        if (!parentDirectoryExist) {
            log.warn("Родительской папки {} не существует", parent);
            throw new ParentDirectoryNotExistException();
        }

        if (directoryToCreateExist) {
            log.warn("Создаваемая папка {} уже существует", fullObject);
            throw new ResourceAlreadyExistException();
        }

        minioService.createDirectory(ROOT_BUCKET, fullObject);
        Resource resource = resourceBuilder.buildDirectory(path);
        searchService.saveUserResourceToDatabase(resource);
        return resourceMapper.convertToDirectoryDto(resource);
    }

    public ResourceDto getResourceData(String path) {
        String fullObject = pathUtil.getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        if (!items.isEmpty()) {
            Resource resource = resourceBuilder.build(items.get(0));
            return resourceMapper.convertToResourceDto(resource);
        } else {
            log.warn("Ресурс {}, по которому запрашиваются данные, не найден", fullObject);
            throw new ResourceNotFoundException();
        }
    }

    public List<ResourceDto> getDirectoryContents(String path) {
        String fullObject = pathUtil.getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        List<ResourceDto> resourceDtos = new ArrayList<>();
        for (Item item : items) {
            if (!item.objectName().equals(fullObject)) {
                Resource resource = resourceBuilder.build(item);
                resourceDtos.add(resourceMapper.convertToResourceDto(resource));
            }
        }
        return resourceDtos;
    }

    public void removeResource(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        String fullObject = pathUtil.getFullObject(path);
        List<Item> deletedItems = minioService.removeObject(ROOT_BUCKET, fullObject);
        deletedItems.forEach(item -> searchService.removeUserResourceFromDatabase(item.objectName().substring(userContextService.getUserRootDirectory().length())));
    }

    public ResourceDto moveOrRenameResource(String from, String to) {
        if (from.startsWith("/"))
            from = from.substring(1);

        if (to.startsWith("/"))
            to = to.substring(1);

        String oldObject = pathUtil.getFullObject(from);
        String newObject = pathUtil.getFullObject(to);
        String parentOfNewObject = pathUtil.getParent(newObject);

        boolean oldObjectExist = minioService.checkResourceExist(ROOT_BUCKET, oldObject);
        boolean parentOfNewObjectExist = minioService.checkResourceExist(ROOT_BUCKET, parentOfNewObject);

        if (!oldObjectExist) {
            log.warn("Старого объекта {} не существует", oldObject);
            throw new ResourceNotFoundException();
        }

        if (!parentOfNewObjectExist) {
            log.warn("Родительской папки нового объекта {} не существует", parentOfNewObject);
            throw new ResourceNotFoundException();
        }

        boolean newFileAlreadyExist = minioService.checkResourceExist(ROOT_BUCKET, newObject);
        if (newFileAlreadyExist) {
            log.warn("Новый файл {} уже существует", newObject);
            throw new ResourceAlreadyExistException();
        }

        List<Item> itemsToChange = minioService.getListObjects(ROOT_BUCKET, oldObject, true);

        itemsToChange.forEach(item -> {
            String newKey = item.objectName().replace(oldObject, newObject);
            minioService.copyObject(ROOT_BUCKET, item.objectName(), newKey);
        });

        List<Item> changedItems = minioService.getListObjects(ROOT_BUCKET, newObject, true);
        changedItems.forEach(changedItem -> {
            Resource resource = resourceBuilder.build(changedItem);
            searchService.saveUserResourceToDatabase(resource);
        });

        itemsToChange.forEach(item -> {
            List<Item> oldItems = minioService.removeObject(ROOT_BUCKET, item.objectName());
            oldItems.forEach(oldItem -> searchService.removeUserResourceFromDatabase(oldItem.objectName().substring(userContextService.getUserRootDirectory().length())));
        });

        Item item = minioService.getListObjects(ROOT_BUCKET, newObject, false).get(0);
        return resourceMapper.convertToResourceDto(resourceBuilder.build(item));
    }

}
