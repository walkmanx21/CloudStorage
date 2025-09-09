package org.walkmanx21.spring.cloudstorage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.walkmanx21.spring.cloudstorage.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
