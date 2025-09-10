package org.walkmanx21.spring.cloudstorage.exceptions;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String message) {
        super("User with this username already exist");
    }
}
