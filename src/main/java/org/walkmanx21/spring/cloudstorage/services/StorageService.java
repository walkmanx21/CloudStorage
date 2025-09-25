package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.walkmanx21.spring.cloudstorage.dto.PathRequestDto;
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

    public Resource createDirectory(PathRequestDto pathRequestDto) {
        Path directory = Paths.get(pathRequestDto.getPath());
        String parent = directory.getParent() == null ? getUserRootDirectory() :  getUserRootDirectory() + directory.getParent().toString().replace("\\", "/") + "/";
        String fullPathToDirectory = getUserRootDirectory() + directory + "/";

        boolean parentDirectoryExist = minioService.checkResourceExist(ROOT_BUCKET, parent);
        boolean directoryToCreateExist = minioService.checkResourceExist(ROOT_BUCKET, fullPathToDirectory);

        if(!parentDirectoryExist)
            throw new ParentDirectoryNotExistException();

        if(directoryToCreateExist)
            throw new ResourceAlreadyExistException();

        minioService.createDirectory(ROOT_BUCKET, fullPathToDirectory.replace("\\", "/"));
        return resourceBuilder.build(directory, fullPathToDirectory, 0L);
    }

    public void createUserRootDirectory(int userId) {
        String userDirectory = "user-" + userId + "-files/";
        minioService.createDirectory(ROOT_BUCKET, userDirectory);
    }

    public Resource getResourceData(String path) {
        String fullPath = getFullPath(path);
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullPath, false);
        Item item;
        if (!items.isEmpty())
            item = items.get(0);
        else
            throw new ResourceNotFoundException();
        return resourceBuilder.build2(path, item);
    }


    public List<Resource> getDirectoryContents(PathRequestDto pathRequestDto){
        Path path = Paths.get(pathRequestDto.getPath());
        String fullPath = getFullPath(pathRequestDto.getPath());
        List<Item> items = minioService.getListObjects(ROOT_BUCKET, fullPath, false);
        List<Resource> resources = new ArrayList<>();
        for (Item item : items) {
            if (!item.objectName().equals(fullPath))
                resources.add(resourceBuilder.build(path, item));
        }
        return resources;
    }

    public void removeResource(PathRequestDto pathRequestDto) {
        String fullPath = getFullPath(pathRequestDto.getPath());
        minioService.removeObject(ROOT_BUCKET, fullPath);
    }

    public List<Resource> uploadResources(PathRequestDto pathRequestDto, List<MultipartFile> files) {
        String destinationDirectory = getFullPath(pathRequestDto.getPath());

        Map<String, MultipartFile> filesMap = new HashMap<>();
        for(MultipartFile file : files) {
            boolean fileExist = minioService.checkResourceExist(ROOT_BUCKET, destinationDirectory + file.getOriginalFilename());

            if (!fileExist)
                filesMap.put(file.getOriginalFilename(), file);
            else
                throw new ResourceAlreadyExistException();
        }

        minioService.uploadResources(ROOT_BUCKET, destinationDirectory, filesMap);
        List<Resource> resources = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            Path path = Paths.get(pathRequestDto.getPath() + file.getOriginalFilename());
            resources.add(resourceBuilder.buildFile(path, file.getSize()));
        });

        return resources;
    }

    public StreamingResponseBody downloadResource(PathRequestDto pathRequestDto) {
        String userDirectory = getUserRootDirectory();
        String fullPath = getFullPath(pathRequestDto.getPath());

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
        String oldObject = getUserRootDirectory() + from;
        String newObject = getUserRootDirectory() + to;
        minioService.copyObject(ROOT_BUCKET, oldObject, newObject);
        Item item = minioService.getListObjects(ROOT_BUCKET, newObject, false).get(0);
        removeResource(new PathRequestDto(from));
        return resourceBuilder.build(to, item);
    }

    private String getUserRootDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "user-" + myUserDetails.getUser().getId() + "-files/";
    }

    private String getFullPath(String path) {
        if (path.contains(getUserRootDirectory()))
            return path;
        else
            return  getUserRootDirectory() + path;
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
