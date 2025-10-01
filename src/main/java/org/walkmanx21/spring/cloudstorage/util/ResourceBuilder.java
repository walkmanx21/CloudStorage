package org.walkmanx21.spring.cloudstorage.util;

import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;
import org.walkmanx21.spring.cloudstorage.models.User;

import java.time.LocalDateTime;

@Component
public class ResourceBuilder {

    public Resource build (String userRootDirectory, User user, Item item) {
        if (item.objectName().endsWith("/")) {
            return buildDirectory(user, item.objectName().substring(userRootDirectory.length()));
        } else {
            return buildFile(user, item.objectName().substring(userRootDirectory.length()), item.size());
        }
    }

    public Resource buildDirectory(User user, String object) {
        return Resource.builder()
                .user(user)
                .object(object)
                .type(ResourceType.DIRECTORY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Resource buildFile(User user, String object, long size) {
        return Resource.builder()
                .user(user)
                .object(object)
                .type(ResourceType.FILE)
                .size(size)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
