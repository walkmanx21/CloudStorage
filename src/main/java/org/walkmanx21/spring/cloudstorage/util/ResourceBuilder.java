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
        String parent = path.getParent() == null ? "/" : path.getParent() + "/";
        if (fullPath.endsWith("/")) {
            return buildDirectory(parent, path);
        } else {
            return buildFile(parent, path, size);
        }
    }

    public Resource build(Path requestPath, Item item) {
        Path path = Paths.get(item.objectName());
        String parent = requestPath == null ? "/" : requestPath + "/";

        if (item.objectName().endsWith("/")) {
            return buildDirectory(parent, path);
        } else {
            return buildFile(parent, path, item.size());
        }
    }

    public Directory buildDirectory(String parent, Path path) {
         return Directory.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceType.DIRECTORY)
                .name(path.getFileName().toString())
                .build();
    }

    public File buildFile(String parent, Path path, long size) {
        return File.builder()
                .path(parent.replace("\\", "/"))
                .type(ResourceType.FILE)
                .name(path.getFileName().toString())
                .size(size)
                .build();
    }
}
