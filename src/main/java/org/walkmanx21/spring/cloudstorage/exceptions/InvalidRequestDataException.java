package org.walkmanx21.spring.cloudstorage.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidRequestDataException extends AuthenticationException {

    public InvalidRequestDataException(String message) {
        super(message);
    }
}
