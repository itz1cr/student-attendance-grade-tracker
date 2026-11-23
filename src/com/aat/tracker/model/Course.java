package com.aat.tracker.model;

import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.util.Validator;

import java.util.Objects;

/**
 * A course (subject) offered in a semester.
 */
public class Course implements Reportable {

    public static final int MIN_CREDITS = 1;
    public static final int MAX_CREDITS = 6;
    public static final int DEFAULT_CREDITS = 3;

    /** Value used for a course that has not been saved to the database yet. */
    public static final int NEW_COURSE_ID = -1;

    private int courseId;
    private final String courseCode;
    private String title;
    private int credits;
    private String instructor;
    private int semester;

    /** Full constructor (Week 4: constructors). */
    public Course(String courseCode, String title, int credits, String instructor, int semester)
            throws ValidationException {
        this.courseId = NEW_COURSE_ID;
        this.courseCode = validateCourseCode(courseCode);
        setTitle(title);
        setCredits(credits);
        setInstructor(instructor);
        setSemester(semester);
    }

    /** Overloaded constructor using the default credits value (Week 4: overloading). */
    public Course(String courseCode, String title, int semester) throws ValidationException {
        this(courseCode, title, DEFAULT_CREDITS, "", semester);
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) throws ValidationException {
        if (courseId < 0) {
            throw new ValidationException("Course id cannot be negative.");
        }
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws ValidationException {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Course title cannot be blank.");
        }
        this.title = title.trim();
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) throws ValidationException {
        if (credits < MIN_CREDITS || credits > MAX_CREDITS) {
            throw new ValidationException(
                    "Credits must be between " + MIN_CREDITS + " and " + MAX_CREDITS + ".");
        }
        this.credits = credits;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor == null ? "" : instructor.trim();
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) throws ValidationException {
        if (semester < Student.MIN_SEMESTER || semester > Student.MAX_SEMESTER) {
            throw new ValidationException("Semester must be between "
                    + Student.MIN_SEMESTER + " and " + Student.MAX_SEMESTER + ".");
        }
        this.semester = semester;
    }

    /** Compact human-friendly label for combo boxes. */
    public String displayName() {
        return courseCode + " - " + title;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Course course)) {
            return false;
        }
        return courseCode.equalsIgnoreCase(course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode.toLowerCase());
    }

    @Override
    public String toString() {
        return displayName();
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{
                courseCode, title, String.valueOf(credits), instructor, String.valueOf(semester)
        };
    }

    private static String validateCourseCode(String courseCode) throws ValidationException {
        if (courseCode == null || courseCode.isBlank()) {
            throw new ValidationException("Course code cannot be blank.");
        }
        String value = courseCode.trim().toUpperCase();
        if (!Validator.matches(value, "[A-Z0-9]{2,12}")) {
            throw new ValidationException("Course code must be 2-12 letters/digits.");
        }
        return value;
    }
}
