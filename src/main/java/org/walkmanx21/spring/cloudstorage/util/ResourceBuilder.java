package org.walkmanx21.spring.cloudstorage.util;

import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.models.Directory;
import org.walkmanx21.spring.cloudstorage.models.File;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ResourceBuilder {

    public Resource build(Path path, String fullPath, Long size) {
        if (fullPath.endsWith("/")) {
            return buildDirectory(path);
        } else {
            return buildFile(path, size);
        }
    }

    public Resource build(Path requestPath, Item item) {
        Path path = Paths.get(item.objectName());

        if (item.objectName().endsWith("/")) {
            return buildDirectory(path);
        } else {
            return buildFile(path, item.size());
        }
    }

    public Resource build(String object, Item item) {
        Path path = Paths.get(object);
        if (item.objectName().endsWith("/")) {
            return buildDirectory(path);
        } else {
            return buildFile(path, item.size());
        }
    }

    public Resource build2(String path, Item item) {
        Path object = Paths.get(path);
        if (path.endsWith("/")) {
            return buildDirectory(object);
        } else {
            return buildFile(object, item.size());
        }
    }

    public Directory buildDirectory(Path object) {
        String parent = object.getParent() == null ? "/" : object.getParent() + "/";
         return Directory.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceType.DIRECTORY)
                .name(object.getFileName().toString() + "/")
                .build();
    }

    public File buildFile(Path object, long size) {
        String parent = object.getParent() == null ? "/" : object.getParent() + "/";
        return File.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceType.FILE)
                .name(object.getFileName().toString())
                .size(size)
                .build();
    }
}
