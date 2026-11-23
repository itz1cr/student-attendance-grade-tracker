package com.aat.tracker.dao;

import com.aat.tracker.exception.DatabaseException;
import com.aat.tracker.model.AttendanceEntry;
import com.aat.tracker.model.AttendanceRecord;
import com.aat.tracker.model.AttendanceStat;
import com.aat.tracker.model.AttendanceStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC data access for attendance (Week 7: CRUD operations, aggregate SQL
 * queries, transactions with rollback).
 */
public class AttendanceDAO extends BaseDao {

    private static final String UPSERT_SQL = """
            INSERT INTO attendance (student_id, course_id, class_date, status, remark)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(student_id, course_id, class_date)
            DO UPDATE SET status = excluded.status, remark = excluded.remark
            """;

    private static final String SELECT_BY_COURSE_AND_DATE = """
            SELECT * FROM attendance WHERE course_id = ? AND class_date = ?
            """;

    private static final String SELECT_HISTORY = """
            SELECT * FROM attendance
            WHERE student_id = ? AND course_id = ?
            ORDER BY class_date
            """;

    private static final String DELETE_BY_COURSE_AND_DATE = """
            DELETE FROM attendance WHERE course_id = ? AND class_date = ?
            """;

    private static final String SELECT_COURSE_STATISTICS = """
            SELECT s.student_id, s.roll_number, s.name, c.course_code,
                   COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS present,
                   COALESCE(SUM(CASE WHEN a.status = 'ABSENT'  THEN 1 ELSE 0 END), 0) AS absent,
                   COALESCE(SUM(CASE WHEN a.status = 'LATE'    THEN 1 ELSE 0 END), 0) AS late,
                   COUNT(a.attendance_id) AS total
            FROM students s
            JOIN enrollments e ON e.student_id = s.student_id AND e.course_id = ?
            JOIN courses c      ON c.course_id  = e.course_id
            LEFT JOIN attendance a ON a.student_id = s.student_id AND a.course_id = e.course_id
            GROUP BY s.student_id
            ORDER BY s.roll_number
            """;

    /** Marks one student's attendance; updates the row when it already exists. */
    public void mark(int studentId, int courseId, LocalDate classDate,
                     AttendanceStatus status, String remark) {
        executeUpdate(UPSERT_SQL, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            statement.setString(3, classDate.toString());
            statement.setString(4, status.name());
            statement.setString(5, remark == null ? "" : remark);
        });
    }

    /**
     * Saves a whole class sheet inside ONE database transaction: either every
     * row is committed or nothing is (rollback on failure).
     */
    public void markClassAttendance(List<AttendanceEntry> entries, int courseId,
                                    LocalDate classDate) {
        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
                for (AttendanceEntry entry : entries) {
                    statement.setInt(1, entry.studentId());
                    statement.setInt(2, courseId);
                    statement.setString(3, classDate.toString());
                    statement.setString(4, entry.status().name());
                    statement.setString(5, entry.remark() == null ? "" : entry.remark());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseException(
                        "Saving the class sheet failed and was rolled back: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not open the attendance transaction.", e);
        }
    }

    /** Already-saved statuses of one class, keyed by student id. */
    public Map<Integer, AttendanceStatus> getStatusMapForDate(int courseId, LocalDate date) {
        List<AttendanceRecord> rows = query(SELECT_BY_COURSE_AND_DATE, statement -> {
            statement.setInt(1, courseId);
            statement.setString(2, date.toString());
        }, AttendanceDAO::mapRow);
        Map<Integer, AttendanceStatus> statuses = new HashMap<>();
        for (AttendanceRecord row : rows) {
            statuses.put(row.getStudentId(), row.getStatus());
        }
        return statuses;
    }

    /** Full attendance history of one student in one course. */
    public List<AttendanceRecord> findHistory(int studentId, int courseId) {
        return query(SELECT_HISTORY, statement -> {
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
        }, AttendanceDAO::mapRow);
    }

    public boolean deleteByCourseAndDate(int courseId, LocalDate date) {
        return executeUpdate(DELETE_BY_COURSE_AND_DATE, statement -> {
            statement.setInt(1, courseId);
            statement.setString(2, date.toString());
        }) > 0;
    }

    /** Aggregated attendance figures for every enrolled student of a course. */
    public List<AttendanceStat> getCourseStatistics(int courseId) {
        return query(SELECT_COURSE_STATISTICS,
                statement -> statement.setInt(1, courseId),
                AttendanceDAO::mapStatRow);
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM attendance");
    }

    private static AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord record = new AttendanceRecord(
                rs.getInt("student_id"),
                rs.getInt("course_id"),
                LocalDate.parse(rs.getString("class_date")),
                AttendanceStatus.fromLabel(rs.getString("status")),
                rs.getString("remark"));
        record.setAttendanceId(rs.getInt("attendance_id"));
        return record;
    }

    private static AttendanceStat mapStatRow(ResultSet rs) throws SQLException {
        int present = rs.getInt("present");
        int absent = rs.getInt("absent");
        int late = rs.getInt("late");
        int total = rs.getInt("total");
        double percentage = total == 0
                ? 0.0
                : (present + late) * 100.0 / total;
        return new AttendanceStat(
                rs.getInt("student_id"),
                rs.getString("roll_number"),
                rs.getString("name"),
                rs.getString("course_code"),
                present, absent, late, total, percentage);
    }
}
