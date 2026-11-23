package com.aat.tracker.model;

import com.aat.tracker.exception.ValidationException;

import java.util.Objects;

/**
 * A student of the institution (Week 5: inheritance - extends the abstract
 * base class {@link Person}).
 *
 * <p>A roll number is final: it identifies a student and never changes
 * (Week 4: final keyword).</p>
 */
public class Student extends Person implements Reportable {

    /** Valid semester range (Week 4: static + final constants). */
    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 8;

    /** Value used for a student that has not been saved to the database yet. */
    public static final int NEW_STUDENT_ID = -1;

    private int studentId;
    private final String rollNumber;
    private String department;
    private int semester;
    private String section;

    /** Full constructor (Week 4: constructors, Week 8: validation via exceptions). */
    public Student(String rollNumber, String name, String email, String phone,
                   String department, int semester, String section) throws ValidationException {
        super(name, email, phone);
        this.studentId = NEW_STUDENT_ID;
        this.rollNumber = validateRollNumber(rollNumber);
        setDepartment(department);
        setSemester(semester);
        setSection(section);
    }

    /** Overloaded convenience constructor (Week 4: overloading). */
    public Student(String rollNumber, String name, String department, int semester)
            throws ValidationException {
        this(rollNumber, name, "", "", department, semester, "");
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) throws ValidationException {
        if (studentId < 0) {
            throw new ValidationException("Student id cannot be negative.");
        }
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) throws ValidationException {
        if (department == null || department.trim().isEmpty()) {
            throw new ValidationException("Department cannot be blank.");
        }
        this.department = department.trim();
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) throws ValidationException {
        if (semester < MIN_SEMESTER || semester > MAX_SEMESTER) {
            throw new ValidationException(
                    "Semester must be between " + MIN_SEMESTER + " and " + MAX_SEMESTER + ".");
        }
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section == null ? "" : section.trim().toUpperCase();
    }

    /** Students are identified by roll number (Week 5: overriding Object methods). */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Student student)) {
            return false;
        }
        return rollNumber.equalsIgnoreCase(student.rollNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rollNumber.toLowerCase());
    }

    @Override
    public String getRoleLabel() {
        return "Student";
    }

    @Override
    public String toString() {
        return rollNumber + " - " + getName();
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{
                rollNumber, getName(), getEmail(), getPhone(), department,
                String.valueOf(semester), section
        };
    }

    private static String validateRollNumber(String rollNumber) throws ValidationException {
        if (rollNumber == null || rollNumber.isBlank()) {
            throw new ValidationException("Roll number cannot be blank.");
        }
        String value = rollNumber.trim().toUpperCase();
        if (value.length() < 2 || value.length() > 20) {
            throw new ValidationException("Roll number must be 2-20 characters.");
        }
        return value;
    }
}
