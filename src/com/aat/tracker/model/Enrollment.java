package com.aat.tracker.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Links a {@link Student} to a {@link Course} (association / "has-a"
 * relationship, Week 4: classes and objects).
 *
 * <p>Persisted in the {@code enrollments} table that resolves the M:N
 * relationship between students and courses.</p>
 */
public class Enrollment {

    public static final int NEW_ENROLLMENT_ID = -1;

    private int enrollmentId;
    private final Student student;
    private final Course course;
    private LocalDate enrolledOn;

    public Enrollment(Student student, Course course, LocalDate enrolledOn) {
        this.enrollmentId = NEW_ENROLLMENT_ID;
        this.student = Objects.requireNonNull(student, "student");
        this.course = Objects.requireNonNull(course, "course");
        this.enrolledOn = enrolledOn == null ? LocalDate.now() : enrolledOn;
    }

    /** Overloaded constructor: enrolment date defaults to today. */
    public Enrollment(Student student, Course course) {
        this(student, course, LocalDate.now());
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }

    @Override
    public String toString() {
        return student.getRollNumber() + " -> " + course.getCourseCode()
                + " (since " + enrolledOn + ")";
    }
}
