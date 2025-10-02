package org.walkmanx21.spring.cloudstorage.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.services.UserContextService;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class PathUtil {

    private final UserContextService userContextService;

    public String getFullObject(String requestObject) {
        String userRootDirectory = userContextService.getUserRootDirectory();
        if (requestObject.startsWith(userRootDirectory))
            return requestObject;
        else
            return (userRootDirectory + requestObject).replace("//", "/");
    }

    public String getParent(String path) {
        String userRootDirectory = userContextService.getUserRootDirectory();
        Path object = Paths.get(path);
        Path parent = object.getParent();
        if (path.startsWith(userRootDirectory))
            return parent.toString().replace("\\", "/") + "/";
        else
            return parent == null ? userRootDirectory : userRootDirectory + parent.toString().replace("\\", "/") + "/";
    }
}
