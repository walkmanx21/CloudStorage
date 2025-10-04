package org.walkmanx21.spring.cloudstorage.services;

import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.walkmanx21.spring.cloudstorage.dto.DirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.DownloadResponseDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ParentDirectoryNotExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.util.ResourceMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioService minioService;
    private final SearchService searchService;
    private final ResourceMapper resourceMapper;
    private final ResourceBuilder resourceBuilder;
    private final UserContextService userContextService;
    private final DownloadService downloadService;
    private final PathService pathService;

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
        String fullObject = pathService.getFullObject(path);
        String parent = pathService.getParent(path);

        validateCreateDirectory(parent, fullObject);

        minioService.createDirectory(ROOT_BUCKET, fullObject);
        Resource resource = resourceBuilder.buildDirectory(path);
        searchService.saveUserResourceToDatabase(resource);
        return resourceMapper.convertToDirectoryDto(resource);
    }

    public ResourceDto getResourceData(String path) {
        String fullObject = pathService.getFullObject(path);
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
        String fullObject = pathService.getFullObject(path);
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
        String fullObject = pathService.getFullObject(path);
        List<Item> deletedItems = minioService.removeObject(ROOT_BUCKET, fullObject);
        deletedItems.forEach(item -> searchService.removeUserResourceFromDatabase(item.objectName().substring(userContextService.getUserRootDirectory().length())));
    }

    public ResourceDto moveOrRenameResource(String from, String to) {

        String oldObject = pathService.getFullObject(pathService.preparePath(from));
        String newObject = pathService.getFullObject(pathService.preparePath(to));
        String parentOfNewObject = pathService.getParent(newObject);

        validateMoveOrRename(oldObject, newObject, parentOfNewObject);

        List<Item> itemsToChange = minioService.getListObjects(ROOT_BUCKET, oldObject, true);

        itemsToChange.forEach(item -> {
            String newKey = item.objectName().replace(oldObject, newObject);
            minioService.copyObject(ROOT_BUCKET, item.objectName(), newKey);
        });

        List<Item> newItems = minioService.getListObjects(ROOT_BUCKET, newObject, true);
        newItems.forEach(newItem -> searchService.saveUserResourceToDatabase(resourceBuilder.build(newItem)));

        List<Item> deletedItems = minioService.removeObject(ROOT_BUCKET, oldObject);
        deletedItems.forEach(deletedItem -> searchService.removeUserResourceFromDatabase(deletedItem.objectName().substring(userContextService.getUserRootDirectory().length())));

        return resourceMapper.convertToResourceDto(resourceBuilder.build(newItems.get(0)));
    }

    public DownloadResponseDto downloadResource(String requestObject) {
        String userDirectory = userContextService.getUserRootDirectory();
        String fullObject = pathService.getFullObject(requestObject);
        String fileName = pathService.getFileName(requestObject);

        validateDownload(fullObject);

        if (fullObject.endsWith("/")) {
            return new DownloadResponseDto(outputStream -> downloadService.downloadDirectory(outputStream, fullObject, userDirectory), fileName);
        } else {
            return new DownloadResponseDto(outputStream -> downloadService.downloadFile(outputStream, fullObject), fileName);
        }

    }

    public List<ResourceDto> uploadResources(String path, List<MultipartFile> files) {
        String fullObject = pathService.getFullObject(path);

        Map<String, MultipartFile> filesMap = new HashMap<>();
        for (MultipartFile file : files) {
            boolean fileExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject + file.getOriginalFilename());

            if (!fileExist) {
                filesMap.put(file.getOriginalFilename(), file);
            } else {
                log.warn("Загружаемый ресурс {} уже существует", file.getOriginalFilename());
                throw new ResourceAlreadyExistException();
            }
        }

        minioService.uploadResources(ROOT_BUCKET, fullObject, filesMap);
        List<ResourceDto> resourceDtos = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            String object = path + file.getOriginalFilename();
            Resource resource = resourceBuilder.buildFile(object, file.getSize());
            resourceDtos.add(resourceMapper.convertToFileDto(resource));
            searchService.saveUserResourceToDatabase(resource);
        });

        return resourceDtos;
    }

    private void validateCreateDirectory(String parent, String fullObject) {
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
    }

    private void validateMoveOrRename(String oldObject, String newObject, String parentOfNewObject) {
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
    }

    private void validateDownload(String fullObject) {
        boolean resourceExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject);
        if (!resourceExist) {
            log.warn("Ресурс {} для скачивания не найден", fullObject);
            throw new ResourceNotFoundException();
        }
    }

}
