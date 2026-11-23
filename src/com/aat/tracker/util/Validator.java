package com.aat.tracker.util;

import com.aat.tracker.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Central input-validation helper (Week 4: static methods; Week 8:
 * user-defined exceptions). All methods are static and the class cannot be
 * instantiated.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\- ()\\.]{7,15}$");

    /** Private constructor: utility class, never instantiated. */
    private Validator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /** True when {@code value} fully matches the regular expression. */
    public static boolean matches(String value, String regex) {
        return value != null && Pattern.matches(regex, value);
    }

    /** Parses a positive integer or throws a user-defined ValidationException. */
    public static int parsePositiveInt(String value, String fieldName)
            throws ValidationException {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new ValidationException(fieldName + " must be a positive number.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a whole number.", e);
        }
    }

    /** Parses a non-negative decimal number (marks) with error chaining. */
    public static double parseMarks(String value, String fieldName)
            throws ValidationException {
        try {
            double parsed = Double.parseDouble(value.trim());
            if (parsed < 0) {
                throw new ValidationException(fieldName + " cannot be negative.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a number.", e);
        }
    }

    /** Asserts that {@code value} lies inside [min, max]. */
    public static void requireRange(double value, double min, double max, String fieldName)
            throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between "
                    + min + " and " + max + ".");
        }
    }

    /** Parses an ISO date (yyyy-mm-dd) with built-in exception handling. */
    public static LocalDate parseDate(String value, String fieldName)
            throws ValidationException {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    fieldName + " must be a valid date in yyyy-mm-dd format.", e);
        }
    }
}
