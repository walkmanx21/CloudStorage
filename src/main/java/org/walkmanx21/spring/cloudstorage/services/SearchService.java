package org.walkmanx21.spring.cloudstorage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.Resource;
import org.walkmanx21.spring.cloudstorage.repositories.ResourceRepository;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ResourceRepository resourceRepository;

    public void saveUserResourceToDatabase(Resource resource) {
        resourceRepository.save(resource);
    }

    public void removeUserResourceFromDatabase(String resource) {
//        Optional<UserResource> userResource = userResourceRepository.findUserResourceByUserAndResource(getCurrentUser(), resource);
//        userResource.ifPresent(userResourceRepository::delete);
    }

    public Resource getUserResourceInfo() {
        //test
        return null;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
