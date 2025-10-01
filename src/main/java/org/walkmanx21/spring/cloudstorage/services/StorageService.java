package org.walkmanx21.spring.cloudstorage.services;

import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.dto.DirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.exceptions.DownloadException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceAlreadyExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ParentDirectoryNotExistException;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;
import org.walkmanx21.spring.cloudstorage.dto.OldResourceDto;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;
import org.walkmanx21.spring.cloudstorage.util.OldResourceDtoBuilder;
import org.walkmanx21.spring.cloudstorage.util.ResourceBuilder;
import org.walkmanx21.spring.cloudstorage.util.ResourceMapper;

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
    private final OldResourceDtoBuilder oldResourceDtoBuilder;
    private final SearchService searchService;
    private static final String ROOT_BUCKET = "user-files";
    private final ResourceMapper resourceMapper;
    private final ResourceBuilder resourceBuilder;

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
        String fullObject = getFullObject(path);
        String parent = getParent(path);

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
        Resource resource = resourceBuilder.buildDirectory(getCurrentUser(), path);
        searchService.saveUserResourceToDatabase(resource);
        return resourceMapper.convertToDirectoryDto(resource);
    }

    public ResourceDto getResourceData(String path) {
        String fullObject = getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        if (!items.isEmpty()) {
            Resource resource = resourceBuilder.build(getCurrentUser(), items.get(0));
            return resourceMapper.convertToResourceDto(resource);
        } else {
            log.warn("Ресурс {}, по которому запрашиваются данные, не найден", fullObject);
            throw new ResourceNotFoundException();
        }
    }

    public List<ResourceDto> getDirectoryContents(String path) {
        String fullObject = getFullObject(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullObject, false);
        List<ResourceDto> resourceDtos = new ArrayList<>();
        for (Item item : items) {
            if (!item.objectName().equals(fullObject)) {
                Resource resource = resourceBuilder.build(getCurrentUser(), item);
                resourceDtos.add(resourceMapper.convertToResourceDto(resource));
            }
        }
        return resourceDtos;
    }

    public void removeResource(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        String fullObject = getFullObject(path);
        List<Item> deletedItems = minioService.removeObject(ROOT_BUCKET, fullObject);
        deletedItems.forEach(item -> searchService.removeUserResourceFromDatabase(item.objectName().substring(getUserRootDirectory().length())));
    }

    public List<OldResourceDto> uploadResources(String path, List<MultipartFile> files) {
        String fullObject = getFullObject(path);

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
        List<OldResourceDto> resources = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            String object = path + file.getOriginalFilename();
            resources.add(oldResourceDtoBuilder.buildFileDto(object, file.getSize()));
//            searchService.saveUserResourceToDatabase(getFullObject(object));
        });

        return resources;
    }

    public StreamingResponseBody downloadResource(String requestObject) {
        String userDirectory = getUserRootDirectory();
        String fullObject = getFullObject(requestObject);

        boolean resourceExist = minioService.checkResourceExist(ROOT_BUCKET, fullObject);
        if (!resourceExist) {
            log.warn("Ресурс {} для скачивания не найден", fullObject);
            throw new ResourceNotFoundException();
        }

        return outputStream -> {
            if (fullObject.endsWith("/")) {
                downloadDirectory(outputStream, fullObject, userDirectory);
            } else {
                downloadFile(outputStream, fullObject);
            }
        };
    }

    public OldResourceDto moveOrRenameResource(String from, String to) {
        if (from.startsWith("/"))
            from = from.substring(1);

        if (to.startsWith("/"))
            to = to.substring(1);

        String oldObject = getFullObject(from);
        String newObject = getFullObject(to);
        String parentOfNewObject = getParent(newObject);

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

        List<Item> items = minioService.getListObjects(ROOT_BUCKET, oldObject, true);

        items.forEach(item -> {
            String newKey = item.objectName().replace(oldObject, newObject);
            minioService.copyObject(ROOT_BUCKET, item.objectName(), newKey);
//            searchService.saveUserResourceToDatabase(newKey);
        });

        items.forEach(item -> {
            List<Item> oldItems = minioService.removeObject(ROOT_BUCKET, item.objectName());
            oldItems.forEach(oldItem -> searchService.removeUserResourceFromDatabase(oldItem.objectName()));
        });

        Item item = minioService.getListObjects(ROOT_BUCKET, newObject, false).get(0);
        return oldResourceDtoBuilder.build(to, item);
    }

    public List<OldResourceDto> searchResources(String query) {
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, getUserRootDirectory(), true);
        items.remove(0);
        List<OldResourceDto> foundResources = new ArrayList<>();
        items.forEach(item -> {
            Path path = Paths.get(item.objectName());
            String fileName = path.getFileName().toString();
            if (fileName.contains(query)) {
                String object = item.objectName().substring(getUserRootDirectory().length());
                foundResources.add(oldResourceDtoBuilder.build(object, item));
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
            return (getUserRootDirectory() + requestObject).replace("//", "/");
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
                        log.warn("Ошибка скачивания файла {}", object.objectName());
                        throw new DownloadException();
                    }
                }
            });
        } catch (IOException e) {
            log.warn("Ошибка скачивания папки {}", fullPath);
            throw new DownloadException();
        }
    }

    private void downloadFile(OutputStream outputStream, String fullPath) {
        try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, fullPath)) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            log.warn("Ошибка скачивания файла {}", fullPath);
            throw new DownloadException();
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

}
