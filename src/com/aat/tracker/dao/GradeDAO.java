package com.aat.tracker.dao;

import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AssessmentType;
import com.aat.tracker.model.Grade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JDBC data access for {@link Grade} entities (Week 7: CRUD operations,
 * upsert with ON CONFLICT).
 */
public class GradeDAO extends BaseDao {

    private static final String UPSERT_SQL = """
            INSERT INTO grades (student_id, course_id, assessment,
                                marks_obtained, max_marks, weight_percent)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(student_id, course_id, assessment)
            DO UPDATE SET marks_obtained = excluded.marks_obtained,
                          max_marks      = excluded.max_marks,
                          weight_percent = excluded.weight_percent,
                          recorded_on    = date('now')
            """;

    private static final String SELECT_BY_STUDENT_AND_COURSE = """
            SELECT * FROM grades
            WHERE student_id = ? AND course_id = ?
            ORDER BY assessment
            """;

    private static final String SELECT_BY_COURSE = """
            SELECT * FROM grades WHERE course_id = ?
            """;

    private static final String SELECT_ALL = "SELECT * FROM grades";

    private static final String DELETE_BY_STUDENT_AND_COURSE = """
            DELETE FROM grades WHERE student_id = ? AND course_id = ?
            """;

    /** Saves a grade; overwrites an existing record for the same assessment. */
    public void save(Grade grade) {
        executeUpdate(UPSERT_SQL, statement -> {
            statement.setInt(1, grade.getStudentId());
            statement.setInt(2, grade.getCourseId());
            statement.setString(3, grade.getAssessment().name());
            statement.setDouble(4, grade.getMarksObtained());
            statement.setDouble(5, grade.getMaxMarks());
            statement.setDouble(6, grade.getWeightPercent());
        });
    }

    public List<Grade> findByStudentAndCourse(int studentId, int courseId) {
        return query(SELECT_BY_STUDENT_AND_COURSE, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
        }, GradeDAO::mapRow);
    }

    public List<Grade> findByCourse(int courseId) {
        return query(SELECT_BY_COURSE, statement -> statement.setInt(1, courseId),
                GradeDAO::mapRow);
    }

    public List<Grade> findAll() {
        return query(SELECT_ALL, GradeDAO::mapRow);
    }

    public boolean deleteByStudentAndCourse(int studentId, int courseId) {
        return executeUpdate(DELETE_BY_STUDENT_AND_COURSE, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
        }) > 0;
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM grades");
    }

    private static Grade mapRow(ResultSet rs) throws SQLException {
        try {
            Grade grade = new Grade(
                    rs.getInt("student_id"),
                    rs.getInt("course_id"),
                    AssessmentType.valueOf(rs.getString("assessment")),
                    rs.getDouble("marks_obtained"),
                    rs.getDouble("max_marks"),
                    rs.getDouble("weight_percent"),
                    LocalDate.parse(rs.getString("recorded_on")));
            grade.setGradeId(rs.getInt("grade_id"));
            return grade;
        } catch (ValidationException | IllegalArgumentException e) {
            throw new DatabaseException("Invalid grade row in the database.", e);
        }
    }
}
