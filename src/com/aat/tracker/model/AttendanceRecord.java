package com.aat.tracker.model;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Attendance of one student for one class of a course on a specific date.
 */
public class AttendanceRecord implements Reportable {

    public static final int NEW_ATTENDANCE_ID = -1;

    private int attendanceId;
    private final int studentId;
    private final int courseId;
    private final LocalDate classDate;
    private AttendanceStatus status;
    private String remark;

    public AttendanceRecord(int studentId, int courseId, LocalDate classDate,
                            AttendanceStatus status, String remark) {
        this.attendanceId = NEW_ATTENDANCE_ID;
        this.studentId = studentId;
        this.courseId = courseId;
        this.classDate = classDate;
        this.status = status;
        this.remark = remark == null ? "" : remark;
    }

    /** Overloaded constructor without a remark (Week 4: overloading). */
    public AttendanceRecord(int studentId, int courseId, LocalDate classDate,
                            AttendanceStatus status) {
        this(studentId, courseId, classDate, status, "");
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public LocalDate getClassDate() {
        return classDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? "" : remark;
    }

    @Override
    public String[] toCsvRow() {
        return new String[]{
                String.valueOf(classDate),
                String.valueOf(courseId),
                String.valueOf(studentId),
                status.name(),
                remark
        };
    }

    @Override
    public String toString() {
        return classDate + " [" + courseId + "] student " + studentId + ": "
                + status + (remark.isEmpty() ? "" : " (" + remark + ")");
    }
}
