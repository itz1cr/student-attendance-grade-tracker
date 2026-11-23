package com.aat.tracker.dao;

import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.Course;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access for {@link Course} entities (Week 7: CRUD operations).
 */
public class CourseDAO extends BaseDao implements CrudRepository<Course, Integer> {

    private static final String INSERT_SQL = """
            INSERT INTO courses (course_code, title, credits, instructor, semester)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE courses
            SET course_code = ?, title = ?, credits = ?, instructor = ?, semester = ?
            WHERE course_id = ?
            """;

    private static final String SELECT_BY_ID = "SELECT * FROM courses WHERE course_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM courses ORDER BY course_code";
    private static final String DELETE_SQL = "DELETE FROM courses WHERE course_id = ?";
    private static final String SEARCH_SQL = """
            SELECT * FROM courses
            WHERE course_code LIKE ? OR title LIKE ? OR instructor LIKE ?
            ORDER BY course_code
            """;

    @Override
    public Course save(Course course) {
        if (course.getCourseId() == Course.NEW_COURSE_ID) {
            int newId = executeInsert(INSERT_SQL, statement -> {
                statement.setString(1, course.getCourseCode());
                statement.setString(2, course.getTitle());
                statement.setInt(3, course.getCredits());
                statement.setString(4, course.getInstructor());
                statement.setInt(5, course.getSemester());
            });
            assignId(course, newId);
        } else {
            executeUpdate(UPDATE_SQL, statement -> {
                statement.setString(1, course.getCourseCode());
                statement.setString(2, course.getTitle());
                statement.setInt(3, course.getCredits());
                statement.setString(4, course.getInstructor());
                statement.setInt(5, course.getSemester());
                statement.setInt(6, course.getCourseId());
            });
        }
        return course;
    }

    @Override
    public Optional<Course> findById(Integer id) {
        List<Course> rows = query(SELECT_BY_ID, statement -> statement.setInt(1, id),
                CourseDAO::mapRow);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public List<Course> findAll() {
        return query(SELECT_ALL, CourseDAO::mapRow);
    }

    /** Case-insensitive search on code, title or instructor. */
    public List<Course> search(String keyword) {
        String pattern = "%" + keyword + "%";
        return query(SEARCH_SQL, statement -> {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
        }, CourseDAO::mapRow);
    }

    @Override
    public boolean deleteById(Integer id) {
        return executeUpdate(DELETE_SQL, statement -> statement.setInt(1, id)) > 0;
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM courses");
    }

    private static Course mapRow(ResultSet rs) throws SQLException {
        try {
            Course course = new Course(
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getInt("credits"),
                    rs.getString("instructor"),
                    rs.getInt("semester"));
            course.setCourseId(rs.getInt("course_id"));
            return course;
        } catch (ValidationException e) {
            throw new DatabaseException("Invalid course row in the database.", e);
        }
    }

    private static void assignId(Course course, int newId) {
        try {
            course.setCourseId(newId);
        } catch (ValidationException e) {
            throw new DatabaseException("Database returned an invalid course id.", e);
        }
    }
}
