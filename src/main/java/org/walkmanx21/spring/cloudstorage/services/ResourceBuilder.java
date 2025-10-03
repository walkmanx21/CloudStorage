package org.walkmanx21.spring.cloudstorage.services;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.models.ResourceType;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ResourceBuilder {

    private final UserContextService userContextService;

    public Resource build (Item item) {
        String userRootDirectory = userContextService.getUserRootDirectory();
        if (item.objectName().endsWith("/")) {
            return buildDirectory(item.objectName().substring(userRootDirectory.length()));
        } else {
            return buildFile(item.objectName().substring(userRootDirectory.length()), item.size());
        }
    }

    public Resource buildDirectory(String object) {
        return Resource.builder()
                .user(userContextService.getCurrentUser())
                .object(object)
                .type(ResourceType.DIRECTORY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Resource buildFile(String object, long size) {
        return Resource.builder()
                .user(userContextService.getCurrentUser())
                .object(object)
                .type(ResourceType.FILE)
                .size(size)
                .createdAt(LocalDateTime.now())
                .build();
    }


}
