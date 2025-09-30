package org.walkmanx21.spring.cloudstorage.util;

import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.dto.OldDirectoryDto;
import org.walkmanx21.spring.cloudstorage.dto.OldFileDto;
import org.walkmanx21.spring.cloudstorage.dto.OldResourceDto;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDtoType;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OldResourceDtoBuilder {

    public OldResourceDto build(String object, Item item) {
        if (item.objectName().endsWith("/")) {
            return buildDirectoryDto(object);
        } else {
            return buildFileDto(object, item.size());
        }
    }


    public OldDirectoryDto buildDirectoryDto(String path) {
        Path object = Paths.get(path);
        String parent = object.getParent() == null ? "/" : object.getParent() + "/";
        return OldDirectoryDto.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceDtoType.DIRECTORY)
                .name(object.getFileName().toString() + "/")
                .build();
    }

    public OldFileDto buildFileDto(String path, long size) {
        Path object = Paths.get(path);
        String parent = object.getParent() == null ? "/" : object.getParent() + "/";
        return OldFileDto.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceDtoType.FILE)
                .name(object.getFileName().toString())
                .size(size)
                .build();
    }
}
