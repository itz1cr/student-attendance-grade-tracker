package com.aat.tracker.service;

import com.aat.tracker.dao.AttendanceDAO;
import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.model.AttendanceEntry;
import com.aat.tracker.model.AttendanceRecord;
import com.aat.tracker.model.AttendanceStat;
import com.aat.tracker.model.AttendanceStatus;
import com.aat.tracker.model.Course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Business rules around attendance: bulk marking, per-course statistics,
 * the 75% attendance rule and low-attendance detection.
 */
public class AttendanceService {

    /** Minimum attendance a student must maintain (college rule, static final). */
    public static final double MINIMUM_ATTENDANCE_PERCENT = 75.0;

    private final AttendanceDAO attendanceDao = new AttendanceDAO();
    private final EnrollmentDAO enrollmentDao = new EnrollmentDAO();
    private final CourseDAO courseDao = new CourseDAO();

    /**
     * Marks a whole class in one transactional bulk save after validating
     * that every student is actually enrolled in the course.
     */
    public void markClassAttendance(int courseId, LocalDate classDate,
                                    List<AttendanceEntry> entries)
            throws ValidationException {
        if (entries == null || entries.isEmpty()) {
            throw new ValidationException("No attendance entries to save.");
        }
        for (AttendanceEntry entry : entries) {
            if (!enrollmentDao.isEnrolled(entry.studentId(), courseId)) {
                throw new ValidationException("Student id " + entry.studentId()
                        + " is not enrolled in this course.");
            }
        }
        attendanceDao.markClassAttendance(entries, courseId, classDate);
    }

    /** Aggregated figures for every student enrolled in one course. */
    public List<AttendanceStat> getCourseStatistics(int courseId) {
        return attendanceDao.getCourseStatistics(courseId);
    }

    /** Aggregated figures for every (student, course) pair in the system. */
    public List<AttendanceStat> getAllStatistics() {
        List<AttendanceStat> all = new ArrayList<>();
        for (Course course : courseDao.findAll()) {
            all.addAll(attendanceDao.getCourseStatistics(course.getCourseId()));
        }
        return all;
    }

    /** Pairs whose attendance is below {@link #MINIMUM_ATTENDANCE_PERCENT}. */
    public List<AttendanceStat> getLowAttendanceStudents() {
        List<AttendanceStat> low = new ArrayList<>();
        for (AttendanceStat stat : getAllStatistics()) {
            if (stat.totalClasses() > 0
                    && stat.attendancePercentage() < MINIMUM_ATTENDANCE_PERCENT) {
                low.add(stat);
            }
        }
        return low;
    }

    /** Saved statuses for one class date, keyed by student id. */
    public Map<Integer, AttendanceStatus> getStatusMapForDate(int courseId, LocalDate date) {
        return attendanceDao.getStatusMapForDate(courseId, date);
    }

    /** Attendance history of one student in one course. */
    public List<AttendanceRecord> getHistory(int studentId, int courseId) {
        return attendanceDao.findHistory(studentId, courseId);
    }
}
