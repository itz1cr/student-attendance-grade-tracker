package com.aat.tracker.dao;

import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.Student;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access for {@link Student} entities
 * (Week 7: CRUD operations with PreparedStatement).
 */
public class StudentDAO extends BaseDao implements CrudRepository<Student, Integer> {

    private static final String INSERT_SQL = """
            INSERT INTO students (roll_number, name, email, phone, department, semester, section)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE students
            SET roll_number = ?, name = ?, email = ?, phone = ?,
                department = ?, semester = ?, section = ?
            WHERE student_id = ?
            """;

    private static final String SELECT_BY_ID = "SELECT * FROM students WHERE student_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM students ORDER BY roll_number";
    private static final String DELETE_SQL = "DELETE FROM students WHERE student_id = ?";
    private static final String SEARCH_SQL = """
            SELECT * FROM students
            WHERE roll_number LIKE ? OR name LIKE ? OR department LIKE ?
            ORDER BY roll_number
            """;

    @Override
    public Student save(Student student) {
        if (student.getStudentId() == Student.NEW_STUDENT_ID) {
            int newId = executeInsert(INSERT_SQL, statement -> {
                statement.setString(1, student.getRollNumber());
                statement.setString(2, student.getName());
                statement.setString(3, student.getEmail());
                statement.setString(4, student.getPhone());
                statement.setString(5, student.getDepartment());
                statement.setInt(6, student.getSemester());
                statement.setString(7, student.getSection());
            });
            assignId(student, newId);
        } else {
            executeUpdate(UPDATE_SQL, statement -> {
                statement.setString(1, student.getRollNumber());
                statement.setString(2, student.getName());
                statement.setString(3, student.getEmail());
                statement.setString(4, student.getPhone());
                statement.setString(5, student.getDepartment());
                statement.setInt(6, student.getSemester());
                statement.setString(7, student.getSection());
                statement.setInt(8, student.getStudentId());
            });
        }
        return student;
    }

    @Override
    public Optional<Student> findById(Integer id) {
        List<Student> rows = query(SELECT_BY_ID, statement -> statement.setInt(1, id),
                StudentDAO::mapRow);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public List<Student> findAll() {
        return query(SELECT_ALL, StudentDAO::mapRow);
    }

    /** Case-insensitive search on roll number, name or department. */
    public List<Student> search(String keyword) {
        String pattern = "%" + keyword + "%";
        return query(SEARCH_SQL, statement -> {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
        }, StudentDAO::mapRow);
    }

    @Override
    public boolean deleteById(Integer id) {
        return executeUpdate(DELETE_SQL, statement -> statement.setInt(1, id)) > 0;
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM students");
    }

    private static Student mapRow(ResultSet rs) throws SQLException {
        try {
            Student student = new Student(
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getInt("semester"),
                    rs.getString("section"));
            student.setStudentId(rs.getInt("student_id"));
            return student;
        } catch (ValidationException e) {
            // Exception chaining: invalid database data surfaces as a DatabaseException.
            throw new DatabaseException("Invalid student row in the database.", e);
        }
    }

    private static void assignId(Student student, int newId) {
        try {
            student.setStudentId(newId);
        } catch (ValidationException e) {
            throw new DatabaseException("Database returned an invalid student id.", e);
        }
    }
}
