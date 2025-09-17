package org.walkmanx21.spring.cloudstorage.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidRequestDataException extends BadCredentialsException {
    public InvalidRequestDataException(String message) {
        super(message);
    }
}
