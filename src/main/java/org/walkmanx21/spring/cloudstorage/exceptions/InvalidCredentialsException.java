package org.walkmanx21.spring.cloudstorage.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidCredentialsException extends BadCredentialsException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
