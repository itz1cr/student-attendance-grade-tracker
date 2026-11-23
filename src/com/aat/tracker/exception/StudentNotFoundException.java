package com.aat.tracker.exception;

/**
 * Thrown when a student is not found by id or roll number
 * (Week 8: user-defined exceptions).
 */
public class StudentNotFoundException extends TrackerException {

    public StudentNotFoundException(int studentId) {
        super("Student not found with id " + studentId + ".");
    }

    public StudentNotFoundException(String rollNumber) {
        super("Student not found with roll number '" + rollNumber + "'.");
    }
}
