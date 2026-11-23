package com.aat.tracker.exception;

/**
 * Root of every user-defined (custom) exception in the application
 * (Week 8: user-defined exceptions). Checked by design so that callers are
 * forced to handle expected error conditions explicitly.
 */
public class TrackerException extends Exception {

    public TrackerException(String message) {
        super(message);
    }

    /** Exception chaining: preserves the original cause (Week 8). */
    public TrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
