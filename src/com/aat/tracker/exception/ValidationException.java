package com.aat.tracker.exception;

/**
 * Thrown when user input fails a business rule, e.g. blank name, invalid
 * email, marks above the maximum (Week 8: user-defined exceptions).
 */
public class ValidationException extends TrackerException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
