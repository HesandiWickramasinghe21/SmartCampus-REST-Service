package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }

    public LinkedResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
