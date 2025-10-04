package org.walkmanx21.spring.cloudstorage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class PathService {

    private final UserContextService userContextService;

    public String getFullObject(String requestObject) {
        String userRootDirectory = userContextService.getUserRootDirectory();
        if (requestObject.isEmpty() || !requestObject.startsWith(userRootDirectory))
            return (userRootDirectory + requestObject).replace("//", "/");
        else
            return requestObject;
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

    public String getFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public String preparePath (String path) {
        if (path.startsWith("/"))
            return path.substring(1);
        else
            return path;
    }
}
