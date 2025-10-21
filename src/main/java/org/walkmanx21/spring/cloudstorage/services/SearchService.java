package org.walkmanx21.spring.cloudstorage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.dto.ResourceDto;
import org.walkmanx21.spring.cloudstorage.exceptions.ResourceNotFoundException;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.repositories.ResourceRepository;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;
import org.walkmanx21.spring.cloudstorage.util.ResourceMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final ResourceBuilder resourceBuilder;
    private final UserContextService userContextService;

    public void saveUserResourceToDatabase(Resource resource) {
        resourceRepository.save(resource);
    }

    public void removeUserResourceFromDatabase(String object) {
        Optional<Resource> userResource = resourceRepository.findResourceByUserAndObject(userContextService.getCurrentUser(), object);
        userResource.ifPresent(resourceRepository::delete);
    }

    public Set<ResourceDto> searchResources(String query) {
        var foundResources = resourceRepository.findResourceByUserAndObjectContains(userContextService.getCurrentUser(), query);
        Set<ResourceDto> resourceDtos = new HashSet<>();
        foundResources.ifPresentOrElse(resources -> resources.forEach(resource -> {
            Path path = Paths.get(resource.getObject());
            Path parent = path.getParent();

            if (path.getFileName().toString().contains(query)) {
                resourceDtos.add(resourceMapper.convertToResourceDto(resource));
            }

            if (parent == null)
                return;

            while (parent.toString().contains(query)) {
                String objectName = parent.getFileName().toString();
                if (objectName.contains(query)) {
                    Resource directory = resourceBuilder.buildDirectory(parent.toString().replace("\\", "/"));
                    resourceDtos.add(resourceMapper.convertToDirectoryDto(directory));
                }
                parent = parent.getParent();
                if (parent == null)
                    return;
                System.out.println();
            }
        }),
                ResourceNotFoundException::new);
        return resourceDtos;
    }

}
