package com.aat.tracker.dao;

import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.Course;
import com.aat.tracker.model.Enrollment;
import com.aat.tracker.model.Student;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JDBC data access for {@link Enrollment} records - the M:N link between
 * students and courses (Week 7: tables and relationships, SQL joins).
 */
public class EnrollmentDAO extends BaseDao {

    private static final String INSERT_SQL = """
            INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)
            """;

    private static final String DELETE_SQL = """
            DELETE FROM enrollments WHERE student_id = ? AND course_id = ?
            """;

    private static final String EXISTS_SQL = """
            SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?
            """;

    private static final String SELECT_ALL_JOIN = """
            SELECT e.enrollment_id, e.enrolled_on,
                   s.student_id, s.roll_number, s.name, s.email, s.phone,
                   s.department, s.semester AS student_semester, s.section,
                   c.course_id, c.course_code, c.title, c.credits, c.instructor,
                   c.semester AS course_semester
            FROM enrollments e
            JOIN students s ON s.student_id = e.student_id
            JOIN courses  c ON c.course_id  = e.course_id
            ORDER BY c.course_code, s.roll_number
            """;

    private static final String SELECT_BY_COURSE_JOIN = """
            SELECT e.enrollment_id, e.enrolled_on,
                   s.student_id, s.roll_number, s.name, s.email, s.phone,
                   s.department, s.semester AS student_semester, s.section,
                   c.course_id, c.course_code, c.title, c.credits, c.instructor,
                   c.semester AS course_semester
            FROM enrollments e
            JOIN students s ON s.student_id = e.student_id
            JOIN courses  c ON c.course_id  = e.course_id
            WHERE e.course_id = ?
            ORDER BY s.roll_number
            """;

    private static final String SELECT_BY_STUDENT_JOIN = """
            SELECT e.enrollment_id, e.enrolled_on,
                   s.student_id, s.roll_number, s.name, s.email, s.phone,
                   s.department, s.semester AS student_semester, s.section,
                   c.course_id, c.course_code, c.title, c.credits, c.instructor,
                   c.semester AS course_semester
            FROM enrollments e
            JOIN students s ON s.student_id = e.student_id
            JOIN courses  c ON c.course_id  = e.course_id
            WHERE e.student_id = ?
            ORDER BY c.course_code
            """;

    /** Enrols a student into a course (insert). */
    public void enroll(int studentId, int courseId) {
        executeInsert(INSERT_SQL, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
        });
    }

    /** Removes the enrolment link (delete). */
    public boolean unenroll(int studentId, int courseId) {
        return executeUpdate(DELETE_SQL, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
        }) > 0;
    }

    public boolean isEnrolled(int studentId, int courseId) {
        List<Integer> counts = query(EXISTS_SQL, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
        }, resultSet -> resultSet.getInt(1));
        return !counts.isEmpty() && counts.get(0) > 0;
    }

    public List<Enrollment> findAll() {
        return query(SELECT_ALL_JOIN, EnrollmentDAO::mapJoinedRow);
    }

    /** Every enrolment of a course, with fully loaded student + course objects. */
    public List<Enrollment> findByCourse(int courseId) {
        return query(SELECT_BY_COURSE_JOIN,
                statement -> statement.setInt(1, courseId),
                EnrollmentDAO::mapJoinedRow);
    }

    /** Every enrolment of a student, with fully loaded student + course objects. */
    public List<Enrollment> findByStudent(int studentId) {
        return query(SELECT_BY_STUDENT_JOIN,
                statement -> statement.setInt(1, studentId),
                EnrollmentDAO::mapJoinedRow);
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM enrollments");
    }

    private static Enrollment mapJoinedRow(ResultSet rs) throws SQLException {
        try {
            Student student = new Student(
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getInt("student_semester"),
                    rs.getString("section"));
            student.setStudentId(rs.getInt("student_id"));

            Course course = new Course(
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getInt("credits"),
                    rs.getString("instructor"),
                    rs.getInt("course_semester"));
            course.setCourseId(rs.getInt("course_id"));

            Enrollment enrollment = new Enrollment(
                    student, course, LocalDate.parse(rs.getString("enrolled_on")));
            enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
            return enrollment;
        } catch (ValidationException e) {
            throw new DatabaseException("Invalid enrolment row in the database.", e);
        }
    }
}
