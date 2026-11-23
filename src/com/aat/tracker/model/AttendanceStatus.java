package com.aat.tracker.model;

import java.util.Locale;

/**
 * Attendance status of a student for one class
 * (Week 8: enumerations).
 */
public enum AttendanceStatus {

    PRESENT("Present", "P"),
    ABSENT("Absent", "A"),
    LATE("Late", "L");

    private final String displayName;
    private final String shortCode;

    AttendanceStatus(String displayName, String shortCode) {
        this.displayName = displayName;
        this.shortCode = shortCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortCode() {
        return shortCode;
    }

    /** Static factory: resolves a label such as "Present" or "PRESENT". */
    public static AttendanceStatus fromLabel(String label) {
        if (label == null) {
            return ABSENT;
        }
        String value = label.trim().toUpperCase(Locale.ROOT);
        for (AttendanceStatus status : values()) {
            if (status.name().equals(value)
                    || status.displayName.toUpperCase(Locale.ROOT).equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown attendance status: " + label);
    }

    /** Overloaded static factory: resolves a short code such as "P". */
    public static AttendanceStatus fromShortCode(String code) {
        for (AttendanceStatus status : values()) {
            if (status.shortCode.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown attendance short code: " + code);
    }

    /** Display text used by combo boxes and table renderers (polymorphic). */
    @Override
    public String toString() {
        return displayName;
    }
}
