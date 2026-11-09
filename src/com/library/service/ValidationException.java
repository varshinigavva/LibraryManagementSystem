package com.library.service;

/**
 * Custom exception thrown when a library business rule or data validation fails.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

