package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SensorUnavailableException(String message) {
        super(message);
    }

    public SensorUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
