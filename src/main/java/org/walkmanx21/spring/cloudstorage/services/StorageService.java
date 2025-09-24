package org.walkmanx21.spring.cloudstorage.services;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
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

import java.io.FileOutputStream;
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

    public Resource getResourceData(PathRequestDto pathRequestDto) {
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        StatObjectResponse stat = minioService.getResourceData(ROOT_BUCKET, fullPath);
        return resourceBuilder.build(Paths.get(pathRequestDto.getPath()), fullPath, stat.size());
    }


    public List<Resource> getDirectoryContents(PathRequestDto pathRequestDto){
        Path path = Paths.get(pathRequestDto.getPath());
        String fullPath = getUserRootDirectory() + pathRequestDto.getPath();
        List<Item> items = minioService.getDirectoryContents(ROOT_BUCKET, fullPath, false);
        List<Resource> resources = new ArrayList<>();
        for (Item item : items) {
            if (!item.objectName().equals(fullPath))
                resources.add(resourceBuilder.build(path, item));
        }
        return resources;
    }

    public void removeResource(PathRequestDto pathRequestDto) {
        String fullPath = (getUserRootDirectory() + pathRequestDto.getPath()).replace("//", "/");
        minioService.removeObject(ROOT_BUCKET, fullPath);
    }

    public List<Resource> uploadResources(PathRequestDto pathRequestDto, List<MultipartFile> files) {
        String destinationDirectory = getUserRootDirectory() + pathRequestDto.getPath();

        Map<String, MultipartFile> filesMap = new HashMap<>();
        for(MultipartFile file : files) {
            boolean fileExist = minioService.checkResourceExist(ROOT_BUCKET, destinationDirectory + file.getOriginalFilename());

            if (fileExist)
                throw new ResourceAlreadyExistException();

            filesMap.put(file.getOriginalFilename(), file);
        }

        minioService.uploadResources(ROOT_BUCKET, destinationDirectory, filesMap);
        List<Resource> resources = new ArrayList<>();
        filesMap.forEach((key, file) -> {
            Path path = Paths.get(pathRequestDto.getPath() + file.getOriginalFilename());
            String parent = path.getParent() == null ? "/" : path.getParent() + "/";
            resources.add(resourceBuilder.buildFile(parent, path, file.getSize()));
        });

        return resources;
    }

    public StreamingResponseBody downloadResource(PathRequestDto pathRequestDto) {
        String userDirectory = getUserRootDirectory();
        String fullPath = (userDirectory + pathRequestDto.getPath()).replace("//", "/");

        boolean resourceExist = minioService.checkResourceExist(ROOT_BUCKET, fullPath);
        if (!resourceExist)
            throw new ResourceNotFoundException();

        return outputStream -> {
            if (fullPath.endsWith("/")) {
//                try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
//                    List<Item> directoryContents = minioService.getDirectoryContents(ROOT_BUCKET, fullPath, true);
//                    directoryContents.forEach(object -> {
//                        if (!object.isDir()) {
//                            String entryName = object.objectName().substring(userDirectory.length());
//                            try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, object.objectName())) {
//                                zipOutputStream.putNextEntry(new ZipEntry(entryName));
//                                inputStream.transferTo(zipOutputStream);
//                                zipOutputStream.closeEntry();
//                            } catch (IOException e) {
//                                throw new DownloadException();
//                            }
//                        }
//                    });
//                }
                downloadDirectory(outputStream, fullPath, userDirectory);
            } else {
//                try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, fullPath)) {
//                    inputStream.transferTo(outputStream);
//                } catch (IOException e) {
//                    throw new DownloadException();
//                }
                downloadFile(outputStream, fullPath);
            }
        };
    }

    private String getUserRootDirectory() {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "user-" + myUserDetails.getUser().getId() + "-files/";
    }

    private ZipOutputStream downloadDirectory(OutputStream outputStream, String fullPath, String userDirectory) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            List<Item> directoryContents = minioService.getDirectoryContents(ROOT_BUCKET, fullPath, true);
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
            return zipOutputStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream downloadFile(OutputStream outputStream, String fullPath) {
        try (InputStream inputStream = minioService.getObject(ROOT_BUCKET, fullPath)) {
            inputStream.transferTo(outputStream);
            return inputStream;
        } catch (IOException e) {
            throw new DownloadException();
        }
    }


}
