package com.aat.tracker.exception;

/**
 * Unchecked wrapper around JDBC/SQL failures. Data-access code never leaks a
 * raw {@link java.sql.SQLException} to the UI; it is caught, wrapped and
 * re-thrown as this runtime exception (Week 8: built-in exception handling
 * combined with user-defined exceptions).
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
