package org.walkmanx21.spring.cloudstorage.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;

@Service
public class UserContextService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    public String getUserRootDirectory() {
        return "user-" + getCurrentUser().getId() + "-files/";
    }
}
