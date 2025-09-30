package org.walkmanx21.spring.cloudstorage.util;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.dto.*;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class ResourceDtoBuilder {

    private final ResourceMapper resourceMapper;

    public ResourceDto build (String userRootDirectory, Item item) {
        if (item.objectName().endsWith("/")) {
            return buildDirectoryDto(userRootDirectory, item);
        } else {
            return buildFileDto(userRootDirectory, item);
        }
    }

    public DirectoryDto buildDirectoryDto(String userRootDirectory, Item item) {
        Resource resource = Resource.builder()
                .object(item.objectName().substring(userRootDirectory.length()))
                .type(ResourceType.DIRECTORY)
                .build();
        return resourceMapper.convertToDirectoryDto(resource);
    }

    public FileDto buildFileDto(String userRootDirectory, Item item) {
        Resource resource = Resource.builder()
                .object(item.objectName().substring(userRootDirectory.length()))
                .type(ResourceType.FILE)
                .size(item.size())
                .build();
        return resourceMapper.convertToFileDto(resource);
    }
}
