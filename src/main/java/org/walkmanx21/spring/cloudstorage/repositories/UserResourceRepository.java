package org.walkmanx21.spring.cloudstorage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.UserResource;

import java.util.Optional;

@Repository
public interface UserResourceRepository extends JpaRepository <UserResource, Integer> {
    Optional<UserResource> findUserResourceByUserAndResource(User user, String resource);
}
