package org.walkmanx21.spring.cloudstorage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.UserResource;
import org.walkmanx21.spring.cloudstorage.repositories.UserResourceRepository;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserResourceRepository userResourceRepository;

    public void writeUserResourceToDatabase(String resource) {
        userResourceRepository.save(new UserResource(getCurrentUser(), resource));
    }

    public void removeUserResourceFromDatabase(String resource) {
        Optional<UserResource> userResource = userResourceRepository.findUserResourceByUserAndResource(getCurrentUser(), resource);
        userResource.ifPresent(userResourceRepository::delete);
    }

    public UserResource getUserResourceInfo() {
        //test
        return null;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
