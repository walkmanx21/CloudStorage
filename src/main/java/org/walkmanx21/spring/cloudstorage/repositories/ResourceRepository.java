package org.walkmanx21.spring.cloudstorage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.Resource;

import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository <Resource, Integer> {
    Optional<Resource> findResourceByUserAndObject(User user, String object);
}
