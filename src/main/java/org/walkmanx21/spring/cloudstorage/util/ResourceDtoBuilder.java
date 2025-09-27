package org.walkmanx21.spring.cloudstorage.util;

import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.dto.DirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.FileDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDtoType;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ResourceDtoBuilder {

    public ResourceDto build(String object, Item item) {
        if (item.objectName().endsWith("/")) {
            return buildDirectoryDto(object);
        } else {
            return buildFileDto(object, item.size());
        }
    }


    public DirectoryDto buildDirectoryDto(String path) {
        Path object = Paths.get(path);
        String parent = object.getParent() == null ? "/" : object.getParent() + "/";
        return DirectoryDto.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceDtoType.DIRECTORY)
                .name(object.getFileName().toString() + "/")
                .build();
    }

    public FileDto buildFileDto(String path, long size) {
        Path object = Paths.get(path);
        String parent = object.getParent() == null ? "/" : object.getParent() + "/";
        return FileDto.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceDtoType.FILE)
                .name(object.getFileName().toString())
                .size(size)
                .build();
    }
}
