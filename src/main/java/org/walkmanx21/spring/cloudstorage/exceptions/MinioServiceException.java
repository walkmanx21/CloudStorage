package org.walkmanx21.spring.cloudstorage.exceptions;

public class MinioServiceException extends RuntimeException {
    public MinioServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
